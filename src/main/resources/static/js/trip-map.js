/*
 * 지도 메뉴 (/map) : 답사 일정을 지도 위에 그리고, 새 장소의 위치를 고른다
 *
 *  - Google Maps API 키가 있으면 : Google 지도 + 장소 검색(Place Autocomplete)
 *      → templates/tripmap/map.html 의 <script ... callback=initTripMap> 가 initTripMap() 을 부른다
 *  - 키가 없거나 Google 지도를 못 불러오면 : Natural Earth 해안선 지도(d3) 로 대신 보여준다
 *
 *  데이터 : GET /map/data (TripMapController) → 날짜별 장소 목록
 */
(function () {
    'use strict';

    // 날짜별 색 (style.css 의 .dc-1 ~ .dc-10 과 같은 순서)
    const DAY_COLORS = ['#4a80ec', '#16a394', '#f08a3c', '#8a63e8', '#e5484d',
        '#0ea5e9', '#c99a06', '#ec4899', '#10b981', '#64748b'];
    const colorOf = dayNo => DAY_COLORS[(dayNo - 1) % DAY_COLORS.length];

    const el = document.getElementById('trip-map');
    if (!el) return;
    const selectedDay = el.dataset.day ? Number(el.dataset.day) : null;
    let dataPromise = null;
    let started = false;

    function loadData() {
        if (!dataPromise) {
            dataPromise = fetch(el.dataset.src, {headers: {'Accept': 'application/json'}}).then(r => r.json());
        }
        return dataPromise;
    }

    // 지도에 보여줄 날 (선택한 날이 있으면 그날만)
    function visibleDays(data) {
        return data.days.filter(d => selectedDay === null || d.dayNo === selectedDay);
    }

    // ================= 장소 추가 폼 =================
    const form = document.getElementById('stop-form');
    const $ = id => document.getElementById(id);
    let onPicked = () => {};     // 지도마다 '고른 위치' 표시 방법이 다르다

    function setPicked(p) {
        if (!form) return;
        $('f-lat').value = p.lat.toFixed(6);
        $('f-lng').value = p.lng.toFixed(6);
        $('f-placeId').value = p.placeId || '';
        $('f-address').value = p.address || '';
        if (p.name) $('f-name').value = p.name;
        $('picked').textContent = `고른 위치 : ${p.name || '지도에서 고른 곳'} (${p.lat.toFixed(4)}, ${p.lng.toFixed(4)})`;
        $('picked').classList.add('on');
        onPicked(p);
    }

    if (form) {
        // 성경 지명에서 고르기
        $('bible-place').addEventListener('change', e => {
            const o = e.target.selectedOptions[0];
            if (!o.value) return;
            setPicked({name: o.value, lat: Number(o.dataset.lat), lng: Number(o.dataset.lng), address: o.dataset.modern});
        });
        // 위치를 안 골랐으면 보내지 않는다
        form.addEventListener('submit', e => {
            if (!$('f-lat').value) {
                e.preventDefault();
                alert('장소를 검색하거나, 성경 지명에서 고르거나, 지도를 눌러 위치를 골라주세요.');
            }
        });
    }

    // ================= 1. Google 지도 =================
    window.initTripMap = async function () {
        if (started) return;
        started = true;
        try {
            const {Map, InfoWindow} = await google.maps.importLibrary('maps');
            const {AdvancedMarkerElement} = await google.maps.importLibrary('marker');
            const places = await google.maps.importLibrary('places');
            const data = await loadData();

            el.classList.add('google');
            const notice = $('tmap-notice');
            if (notice) notice.remove();

            const map = new Map(el, {
                center: {lat: 38.6, lng: 28.5}, zoom: 6,
                mapId: el.dataset.mapId || 'DEMO_MAP_ID',
                mapTypeControl: false, streetViewControl: false,
                gestureHandling: 'cooperative'          // 휴대폰에서 한 손가락은 페이지 스크롤
            });
            const info = new InfoWindow();
            const bounds = new google.maps.LatLngBounds();

            visibleDays(data).forEach(day => {
                const color = colorOf(day.dayNo);
                const path = day.stops.map(s => ({lat: s.lat, lng: s.lng}));
                if (path.length > 1) {
                    new google.maps.Polyline({
                        map, path, geodesic: true, strokeColor: color, strokeOpacity: .85, strokeWeight: 3,
                        icons: [{icon: {path: google.maps.SymbolPath.FORWARD_OPEN_ARROW, scale: 2.5}, offset: '55%', repeat: '120px'}]
                    });
                }
                day.stops.forEach(s => {
                    const marker = new AdvancedMarkerElement({
                        map, position: {lat: s.lat, lng: s.lng}, title: s.name,
                        content: pinElement(s.order, color, s.visited)
                    });
                    marker.addListener('gmp-click', () => {
                        info.setContent(infoContent(day, s));
                        info.open({map, anchor: marker});
                    });
                    bounds.extend({lat: s.lat, lng: s.lng});
                });
            });
            if (!bounds.isEmpty()) {
                map.fitBounds(bounds, 60);
                // 장소가 하나뿐이면 너무 가까워지지 않게
                google.maps.event.addListenerOnce(map, 'idle', () => { if (map.getZoom() > 14) map.setZoom(14); });
            }

            loadDriving();

            if (!form) return;

            // 고른 위치 표시용 임시 마커
            const temp = new AdvancedMarkerElement({map: null, content: pinElement('+', '#191f28', false)});
            onPicked = p => {
                temp.position = {lat: p.lat, lng: p.lng};
                temp.map = map;
                map.panTo({lat: p.lat, lng: p.lng});
            };

            // 지도 클릭 → 위치 고르기 (가게·명소 아이콘을 누르면 그 장소 정보까지)
            map.addListener('click', async e => {
                if (e.placeId) {
                    e.stop();     // 기본 정보창 대신 폼에 채운다
                    try {
                        const place = new places.Place({id: e.placeId, requestedLanguage: 'ko'});
                        await place.fetchFields({fields: ['displayName', 'formattedAddress', 'location']});
                        setPicked({name: place.displayName, lat: place.location.lat(), lng: place.location.lng(),
                            placeId: e.placeId, address: place.formattedAddress});
                        return;
                    } catch (err) {
                        console.warn(err);
                    }
                }
                setPicked({lat: e.latLng.lat(), lng: e.latLng.lng()});
            });

            // 장소 검색창 (Places API (New) 의 PlaceAutocompleteElement)
            if (places.PlaceAutocompleteElement) {
                const ac = new places.PlaceAutocompleteElement({
                    includedRegionCodes: ['tr', 'gr', 'it', 'mt', 'cy', 'il', 'lb', 'kr']
                });
                ac.setAttribute('placeholder', '장소 검색 (예: 에베소 유적지, 아테네 아레오바고)');
                $('place-search').appendChild(ac);
                const pick = async place => {
                    await place.fetchFields({fields: ['displayName', 'formattedAddress', 'location', 'id']});
                    setPicked({name: place.displayName, lat: place.location.lat(), lng: place.location.lng(),
                        placeId: place.id, address: place.formattedAddress});
                };
                // 새 이벤트 이름(gmp-select)과 예전 이름(gmp-placeselect) 모두 받는다
                ac.addEventListener('gmp-select', e => pick(e.placePrediction.toPlace()));
                ac.addEventListener('gmp-placeselect', e => pick(e.place));
            }
        } catch (err) {
            console.error(err);
            fallback('Google 지도를 불러오지 못해서 Natural Earth 지도로 보여드려요. API 키와 사용 설정을 확인해주세요.');
        }
    };

    // 키가 틀렸거나 API 가 꺼져 있으면 Google 이 이 함수를 부른다
    window.gm_authFailure = function () {
        fallback('Google Maps API 키를 확인해주세요. (Maps JavaScript API, Places API (New) 사용 설정 필요) 지금은 Natural Earth 지도로 보여드려요.');
    };

    function pinElement(text, color, visited) {
        const div = document.createElement('div');
        div.className = 'gm-pin' + (visited ? ' visited' : '');
        div.style.setProperty('--c', color);
        div.textContent = visited ? '✓' : text;
        return div;
    }

    // 정보창 : 사용자가 쓴 글이 들어가므로 innerHTML 대신 textContent 로 만든다
    function infoContent(day, s) {
        const box = document.createElement('div');
        box.className = 'gm-info';
        const add = (tag, text, cls) => {
            if (!text) return;
            const n = document.createElement(tag);
            n.textContent = text;
            if (cls) n.className = cls;
            box.appendChild(n);
        };
        add('small', `${day.dayNo}일차 · ${s.order}번째${s.time ? ' · ' + s.time : ''}${s.visited ? ' · 다녀옴' : ''}`);
        add('b', s.name);
        add('div', s.address, 'addr');
        add('div', s.memo, 'memo');
        const a = document.createElement('a');
        a.href = s.googleUrl;
        a.target = '_blank';
        a.rel = 'noopener';
        a.textContent = 'Google 지도에서 열기 ↗';
        box.appendChild(a);
        return box;
    }

    // ================= 2. Natural Earth 지도 (키가 없을 때) =================
    function fallback(message) {
        if (el.dataset.fallback) return;
        el.dataset.fallback = 'on';
        started = true;
        if (message) {
            let n = $('tmap-notice');
            if (!n) {
                n = document.createElement('p');
                n.id = 'tmap-notice';
                n.className = 'tmap-notice';
                el.after(n);
            }
            n.textContent = message;
        }
        if (typeof d3 === 'undefined') return;
        Promise.all([d3.json(el.dataset.land), loadData()]).then(([land, data]) => drawFallback(land, data));
    }

    function drawFallback(land, data) {
        const W = el.clientWidth || 800, H = el.clientHeight || 420;   // 화면 크기 그대로 (글자 크기 유지)
        const days = visibleDays(data);
        const all = days.flatMap(d => d.stops);

        // 장소가 있으면 장소에 맞추고, 없으면 튀르키예 · 그리스 전체
        let lon0 = 19.5, lon1 = 36, lat0 = 34.5, lat1 = 42.3;
        if (all.length) {
            lon0 = d3.min(all, s => s.lng); lon1 = d3.max(all, s => s.lng);
            lat0 = d3.min(all, s => s.lat); lat1 = d3.max(all, s => s.lat);
            const cx = (lon0 + lon1) / 2, cy = (lat0 + lat1) / 2;
            const dx = Math.max((lon1 - lon0) / 2, 1.2), dy = Math.max((lat1 - lat0) / 2, .8);
            lon0 = cx - dx; lon1 = cx + dx; lat0 = cy - dy; lat1 = cy + dy;
        }
        const projection = d3.geoMercator().fitExtent([[40, 40], [W - 40, H - 40]],
            {type: 'MultiPoint', coordinates: [[lon0, lat0], [lon1, lat1]]});

        el.innerHTML = '';
        el.classList.add('fallback');
        const svg = d3.select(el).append('svg').attr('viewBox', `0 0 ${W} ${H}`);
        svg.append('rect').attr('class', 'sea').attr('width', W).attr('height', H);
        const gLand = svg.append('g').attr('class', 'land');
        gLand.append('path').attr('d', d3.geoPath(projection)(land));
        const gRoutes = svg.append('g');
        const gPins = svg.append('g');
        const gTemp = svg.append('g');

        const line = d3.line();
        const routes = gRoutes.selectAll('path').data(days.filter(d => d.stops.length > 1)).join('path')
            .attr('class', 'troute').attr('stroke', d => colorOf(d.dayNo));

        const stops = days.flatMap(d => d.stops.map(s => Object.assign({dayNo: d.dayNo}, s)));
        const pins = gPins.selectAll('g').data(stops).join('g').attr('class', d => 'tpin' + (d.visited ? ' visited' : ''));
        pins.append('circle').attr('r', 10).attr('fill', d => d.visited ? '#fff' : colorOf(d.dayNo)).attr('stroke', d => colorOf(d.dayNo));
        pins.append('text').attr('dy', '0.35em').attr('fill', d => d.visited ? colorOf(d.dayNo) : '#fff')
            .text(d => d.visited ? '✓' : d.order);
        pins.append('title').text(d => `${d.dayNo}일차 ${d.order}. ${d.name}${d.time ? ' · ' + d.time : ''}`);
        const labels = gPins.selectAll('text.tname').data(stops).join('text').attr('class', 'tname').text(d => d.name);

        let t = d3.zoomIdentity;
        let picked = null;
        function render() {
            gLand.attr('transform', t);
            const xy = s => t.apply(projection([s.lng, s.lat]));
            routes.attr('d', d => line(d.stops.map(xy)));
            pins.attr('transform', d => `translate(${xy(d)})`);
            labels.attr('x', d => xy(d)[0] + 13).attr('y', d => xy(d)[1] + 4);
            gTemp.selectAll('*').remove();
            if (picked) {
                const [x, y] = xy(picked);
                gTemp.append('circle').attr('class', 'tpick').attr('cx', x).attr('cy', y).attr('r', 7);
            }
        }
        const zoom = d3.zoom().scaleExtent([1, 12])
            .filter(e => (e.type === 'wheel' ? e.ctrlKey : e.type.startsWith('touch') ? e.touches.length > 1 : !e.button))
            .on('zoom', e => { t = e.transform; render(); });
        svg.call(zoom).on('dblclick.zoom', null);
        render();

        // 지도 클릭 → 위치 고르기
        if (form) {
            svg.on('click', e => {
                if (e.defaultPrevented) return;      // 드래그 후 클릭은 무시
                const [x, y] = d3.pointer(e, svg.node());
                const [lng, lat] = projection.invert(t.invert([x, y]));
                setPicked({lat, lng});
            });
            onPicked = p => { picked = p; render(); };
        }

        const ctrl = d3.select(el).append('div').attr('class', 'jmap-ctrl');
        ctrl.append('button').attr('type', 'button').attr('aria-label', '확대').text('+')
            .on('click', () => svg.transition().duration(250).call(zoom.scaleBy, 1.6));
        ctrl.append('button').attr('type', 'button').attr('aria-label', '축소').text('−')
            .on('click', () => svg.transition().duration(250).call(zoom.scaleBy, 1 / 1.6));
        d3.select(el).append('div').attr('class', 'jmap-credit').text('지도 데이터 © Natural Earth');
    }

    // ================= 3. 날씨 (선택한 날의 장소마다) =================
    // GET /map/weather?lat=&lng=&date= → WeatherService (Open-Meteo)
    function loadWeather() {
        document.querySelectorAll('.stop-list .weather').forEach(box => {
            const q = new URLSearchParams({lat: box.dataset.lat, lng: box.dataset.lng, date: box.dataset.date});
            fetch(el.dataset.weather + '?' + q, {headers: {'Accept': 'application/json'}})
                .then(r => r.status === 200 ? r.json() : null)
                .then(w => {
                    if (!w) return;
                    const part = (label, icon, text) => {
                        const span = document.createElement('span');
                        const b = document.createElement('b');
                        b.textContent = label;
                        span.append(b, ' ' + [icon, text].filter(Boolean).join(' '));
                        box.appendChild(span);
                    };
                    if (w.nowTemp !== null) part('지금', w.nowIcon, `${w.nowTemp}° ${w.nowText || ''}`);
                    if (w.dayMax !== null) {
                        const rain = w.dayRain ? ` · ${w.dayRain}mm` : '';
                        part(w.dayLabel, w.dayIcon, `${w.dayMin}° / ${w.dayMax}° ${w.dayText || ''}${rain}`);
                    }
                })
                .catch(() => {});
        });
    }

    // ================= 4. 차로 이동 거리 (Google Routes, 키가 있을 때만) =================
    // 'Routes API' 를 켜 두면 직선 거리 옆에 '차로 ○km · ○분' 이 붙는다. 실패하면 직선 거리만 보여준다.
    async function loadDriving() {
        let Route;
        try {
            ({Route} = await google.maps.importLibrary('routes'));
        } catch (e) {
            return;
        }
        if (!Route || !Route.computeRoutes) return;
        for (const span of document.querySelectorAll('.stop-list .drive')) {
            const [lat1, lng1] = span.dataset.from.split(',').map(Number);
            const [lat2, lng2] = span.dataset.to.split(',').map(Number);
            try {
                const {routes} = await Route.computeRoutes({
                    origin: {lat: lat1, lng: lng1}, destination: {lat: lat2, lng: lng2},
                    travelMode: 'DRIVING', fields: ['distanceMeters', 'durationMillis']
                });
                if (!routes || !routes.length) continue;
                const km = routes[0].distanceMeters / 1000;
                const min = Math.round(routes[0].durationMillis / 60000);
                const time = min >= 60 ? `${Math.floor(min / 60)}시간 ${min % 60}분` : `${min}분`;
                span.textContent = `차로 ${km < 100 ? km.toFixed(1) : Math.round(km)}km · ${time}`;
            } catch (e) {
                console.info('차로 이동 거리를 가져오지 못했어요 (Routes API 사용 설정 확인)', e.message);
                return;
            }
        }
    }

    loadWeather();

    // 키가 없으면 바로 Natural Earth 지도로
    if (el.dataset.hasKey !== 'true') {
        fallback(null);
    }
})();
