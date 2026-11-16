/*
 * 묵상 여정 지도
 *  - 바탕 지도 : Natural Earth 해안선 (/data/natural-earth-land.json, 퍼블릭 도메인)
 *  - 그리기    : d3 (/js/lib/d3.v7.min.js)
 *  - 데이터    : GET /study/journey-map (StudyController) → 경로, 장소 좌표, 회원 위치
 *
 * 사용법 (Thymeleaf)
 *   <div class="jmap" th:data-src="@{/study/journey-map}" th:data-land="@{/data/natural-earth-land.json}"
 *        th:data-week-link="@{/study(view='week')}" data-focus-week="6"></div>
 *   data-focus-week 가 있으면 그 주의 경로를 크게 보여준다.
 */
(function () {
    'use strict';

    let W = 800, H = 500;     // 실제 크기는 draw() 에서 상자 크기로 다시 정한다

    // 전도여행별 색 (1차, 2차, 3차, 로마 호송)
    const COLORS = {1: '#4a80ec', 2: '#16a394', 3: '#f08a3c', 4: '#8a63e8'};

    // 바다·지방 이름 [이름, 경도, 위도, 종류]
    const LABELS = [
        ['지중해', 21.5, 33.7, 'sea'], ['에게해', 25.0, 38.9, 'sea'], ['아드리아해', 17.2, 42.4, 'sea'],
        ['흑해', 33.5, 42.6, 'sea'],
        ['아시아', 28.6, 38.75, 'land'], ['갈라디아', 33.3, 39.6, 'land'], ['아가야', 21.9, 37.35, 'land'],
        ['이달리야', 16.3, 40.7, 'land'], ['그레데', 25.3, 35.45, 'land'], ['수리아', 36.6, 35.1, 'land']
    ];

    // 지도 전체 범위 (서쪽 로마 ~ 동쪽 안디옥)
    const EXTENT = {type: 'MultiPoint', coordinates: [[11.6, 30.9], [37.0, 42.9]]};

    function init() {
        const maps = document.querySelectorAll('.jmap');
        if (!maps.length || typeof d3 === 'undefined') {
            return;
        }
        const first = maps[0];
        Promise.all([d3.json(first.dataset.land), d3.json(first.dataset.src)])
            .then(([land, data]) => maps.forEach(el => draw(el, land, data)))
            .catch(err => {
                console.error(err);
                maps.forEach(el => el.innerHTML = '<p class="jmap-empty">지도를 불러오지 못했어요.</p>');
            });
    }

    function draw(el, land, data) {
        // 화면 크기 그대로 그려야 휴대폰에서도 글자가 작아지지 않는다
        W = el.clientWidth || 800;
        H = el.clientHeight || 500;
        const focusWeek = el.dataset.focusWeek ? Number(el.dataset.focusWeek) : null;
        const weekLink = el.dataset.weekLink || null;
        const coord = name => {
            const p = data.places[name];
            return p ? [p.lng, p.lat] : null;
        };

        // ---------- 1. 경로 정리 ----------
        // 주마다 [앞 주의 도착지] + 이번 주 장소들 → 지도에서 선이 끊기지 않게
        const myPin = data.pins.find(p => p.me) || {weekNo: 0, stopIndex: 0};
        let prevLast = null;
        const weeks = data.weeks.map(w => {
            const names = w.stops.slice();
            let offset = 0;
            if (prevLast && prevLast !== names[0] && !samePlace(prevLast, names[0])) {
                names.unshift(prevLast);
                offset = 1;
            }
            prevLast = w.stops[w.stops.length - 1];
            // 내가 이 주의 경로에서 몇 번째 장소까지 왔는지 (-1 = 아직, Infinity = 지나감)
            let reached = -1;
            if (w.weekNo < myPin.weekNo) reached = Infinity;
            else if (w.weekNo === myPin.weekNo) reached = myPin.stopIndex + offset;
            return Object.assign({}, w, {names, reached});
        });

        function samePlace(a, b) {
            const pa = data.places[a], pb = data.places[b];
            return pa && pb && pa.lat === pb.lat && pa.lng === pb.lng;
        }

        // ---------- 2. 투영 (Mercator) ----------
        const projection = d3.geoMercator();
        const focus = focusWeek ? weeks.find(w => w.weekNo === focusWeek) : null;
        if (focus) {
            // 그 주의 경로가 화면에 꽉 차도록 (너무 가까우면 조금 넓게)
            const pts = focus.stops.map(coord).filter(Boolean);   // 앞 주 도착지는 빼고 맞춘다
            const lons = pts.map(p => p[0]), lats = pts.map(p => p[1]);
            const cx = (d3.min(lons) + d3.max(lons)) / 2, cy = (d3.min(lats) + d3.max(lats)) / 2;
            const dx = Math.max((d3.max(lons) - d3.min(lons)) / 2, 2.4), dy = Math.max((d3.max(lats) - d3.min(lats)) / 2, 1.5);
            projection.fitExtent([[40, 40], [W - 40, H - 40]],
                {type: 'MultiPoint', coordinates: [[cx - dx, cy - dy], [cx + dx, cy + dy]]});
        } else {
            projection.fitExtent([[0, 0], [W, H]], EXTENT);
        }
        const geoPath = d3.geoPath(projection);
        const base = name => {
            const c = coord(name);
            return c ? projection(c) : null;
        };

        // ---------- 3. SVG ----------
        el.innerHTML = '';
        const svg = d3.select(el).append('svg')
            .attr('viewBox', `0 0 ${W} ${H}`)
            .attr('role', 'img')
            .attr('aria-label', '바울의 전도여행 경로 지도');
        svg.append('rect').attr('class', 'sea').attr('width', W).attr('height', H);

        const gLand = svg.append('g').attr('class', 'land');
        gLand.append('path').attr('d', geoPath(land));

        const gLabels = svg.append('g').attr('class', 'area-labels');
        const gRoutes = svg.append('g').attr('class', 'routes');
        const gStops = svg.append('g').attr('class', 'stops');
        const gNames = svg.append('g').attr('class', 'names');
        const gPins = svg.append('g').attr('class', 'pins');

        // 지방·바다 이름
        const areaLabels = gLabels.selectAll('text').data(W < 500 ? LABELS.filter(d => d[3] === 'sea') : LABELS).join('text')
            .attr('class', d => d[3])
            .text(d => d[0]);

        // 경로 : 회색(아직) 위에 색깔(내가 읽은 만큼)
        const line = d3.line().curve(d3.curveCatmullRom.alpha(0.5));
        const routeData = [];
        weeks.forEach(w => {
            const dim = focus && w.weekNo !== focus.weekNo;
            routeData.push({w, part: 'todo', names: w.names, dim});
            if (w.reached >= 0) {
                const n = w.reached === Infinity ? w.names.length : w.reached + 1;
                if (n >= 2) routeData.push({w, part: 'done', names: w.names.slice(0, n), dim});
            }
        });
        const routes = gRoutes.selectAll('path').data(routeData).join('path')
            .attr('class', d => `route j${d.w.journey} ${d.part} ${d.dim ? 'dim' : ''} ${focus && !d.dim ? 'focus' : ''}`)
            .attr('stroke', d => COLORS[d.w.journey]);
        routes.append('title').text(d => `${d.w.weekNo}주차 · ${d.w.stops.join(' → ')}`);
        if (weekLink && !focus) {
            routes.style('cursor', 'pointer')
                .on('click', (e, d) => location.href = weekLink + '&week=' + d.w.weekNo);
        }

        // 장소 점 (같은 장소는 한 번만)
        const stopMap = new Map();
        weeks.forEach(w => w.names.forEach((name, i) => {
            const c = coord(name);
            if (!c) return;
            const key = c.join(',');
            const done = w.reached === Infinity || i <= w.reached;
            const inFocus = focus && w.weekNo === focus.weekNo;
            const s = stopMap.get(key) || {name, place: data.places[name], journey: w.journey, done: false, inFocus: false, order: null};
            s.done = s.done || done;
            if (inFocus) {
                s.inFocus = true;
                s.journey = w.journey;
                if (s.order === null) s.order = i + 1 - (focus.names.length - focus.stops.length);
                s.name = name;
            }
            stopMap.set(key, s);
        }));
        const stops = Array.from(stopMap.values());
        const stopDots = gStops.selectAll('circle').data(stops).join('circle')
            .attr('class', d => `stop ${d.done ? 'done' : ''} ${focus && !d.inFocus ? 'dim' : ''}`)
            .attr('stroke', d => COLORS[d.journey])
            .attr('fill', d => d.done ? COLORS[d.journey] : '#fff');
        stopDots.append('title').text(d => `${d.name} · ${d.place.modern}`);

        // 장소 이름 : 전체 지도에서는 주요 도시만, 주차 지도에서는 그 주의 장소 모두
        const named = stops.filter(s => focus ? s.inFocus : s.place.major);
        const nameText = gNames.selectAll('text').data(named).join('text')
            .attr('class', d => `name ${d.inFocus ? 'focus' : ''}`)
            .text(d => (focus && d.order > 0 ? d.order + '. ' : '') + d.name.replace(/\(.*\)/, ''));

        // ---------- 4. 회원 위치 (프로필 동그라미) ----------
        const byPlace = d3.group(data.pins, p => p.place);
        const pinGroups = [];
        byPlace.forEach((list, place) => {
            list.sort((a, b) => (b.me ? 1 : 0) - (a.me ? 1 : 0));   // 나를 맨 앞에
            pinGroups.push({place, list: list.slice(0, 4), more: Math.max(0, list.length - 4)});
        });
        const R = 12;
        const pinG = gPins.selectAll('g.pin-group').data(pinGroups).join('g').attr('class', 'pin-group');
        pinG.append('title').text(d => `${d.place} : ${d.list.map(p => p.name).join(', ')}${d.more ? ' 외 ' + d.more + '명' : ''}`);
        pinG.each(function (grp) {
            const g = d3.select(this);
            grp.list.forEach((p, i) => {
                const a = g.append('g').attr('class', 'pin' + (p.me ? ' me' : ''))
                    .attr('transform', `translate(${i * (R * 1.35)},0)`);
                a.append('circle').attr('r', R + 2).attr('class', 'ring');
                if (p.photo) {
                    const id = 'clip-' + Math.random().toString(36).slice(2);
                    a.append('clipPath').attr('id', id).append('circle').attr('r', R);
                    a.append('image').attr('href', p.photo).attr('x', -R).attr('y', -R)
                        .attr('width', R * 2).attr('height', R * 2)
                        .attr('preserveAspectRatio', 'xMidYMid slice').attr('clip-path', `url(#${id})`);
                } else {
                    a.append('circle').attr('r', R).attr('class', 'face');
                    a.append('text').attr('class', 'initials').attr('dy', '0.35em').text(p.initials);
                }
            });
            if (grp.more) {
                g.append('text').attr('class', 'more').attr('x', grp.list.length * R * 1.35 + 2).attr('dy', '0.35em')
                    .text('+' + grp.more);
            }
        });

        // ---------- 5. 확대 / 이동 ----------
        // 확대해도 글자·점 크기는 그대로 두고 위치만 다시 계산한다 (semantic zoom)
        function render(t) {
            const k = t.k;
            gLand.attr('transform', t);
            const xy = (lng, lat) => t.apply(projection([lng, lat]));
            areaLabels.attr('x', d => xy(d[1], d[2])[0]).attr('y', d => xy(d[1], d[2])[1]);
            routes.attr('d', d => line(d.names.map(base).filter(Boolean).map(p => t.apply(p))));
            stopDots.attr('cx', d => xy(d.place.lng, d.place.lat)[0]).attr('cy', d => xy(d.place.lng, d.place.lat)[1])
                .attr('r', d => (d.inFocus ? 5 : 3.2) * Math.min(1.4, Math.sqrt(k)));
            // 프로필 동그라미는 장소 점 위쪽에
            const taken = [];
            pinG.attr('transform', d => {
                const p = data.places[d.place];
                const [x, y] = xy(p.lng, p.lat);
                const n = d.list.length + (d.more ? 1 : 0);
                taken.push({x0: x - 4 - R, x1: x - 4 + (n - 1) * R * 1.35 + R, y0: y - 2 * R - 12, y1: y - 8});
                return `translate(${x - 4},${y - R - 10})`;
            });
            // 이름표 : 오른쪽 위 → 왼쪽 위 → 오른쪽 아래 → 왼쪽 아래 중 겹치지 않는 자리
            nameText.each(function (d) {
                const [x, y] = xy(d.place.lng, d.place.lat);
                const size = d.inFocus ? 15 : 12;
                const w = this.textContent.length * size * 0.92, h = size + 2;
                const spots = [[x + 7, y - 6, 'start'], [x - 7, y - 6, 'end'], [x + 7, y + size + 4, 'start'], [x - 7, y + size + 4, 'end']];
                const boxOf = ([tx, ty, a]) => ({x0: a === 'start' ? tx : tx - w, x1: a === 'start' ? tx + w : tx, y0: ty - h + 2, y1: ty + 2});
                const free = b => b.x0 >= 0 && b.x1 <= W && !taken.some(o => b.x0 < o.x1 && b.x1 > o.x0 && b.y0 < o.y1 && b.y1 > o.y0);
                let spot = spots.find(sp => free(boxOf(sp)));
                // 전체 지도에서 자리가 없으면 이름을 숨긴다 (휴대폰에서 글자가 겹치지 않게)
                if (!spot && !focus) {
                    d3.select(this).style('display', 'none');
                    return;
                }
                spot = spot || spots.find(sp => { const b = boxOf(sp); return b.x0 >= 0 && b.x1 <= W; }) || spots[0];
                taken.push(boxOf(spot));
                d3.select(this).style('display', null).attr('x', spot[0]).attr('y', spot[1]).attr('text-anchor', spot[2]);
            });
        }

        const zoom = d3.zoom().scaleExtent([1, 10])
            .translateExtent([[-W * 0.2, -H * 0.2], [W * 1.2, H * 1.2]])
            // 페이지 스크롤을 막지 않도록 : 휠은 Ctrl + 휠만, 휴대폰은 두 손가락일 때만 지도가 움직인다
            .filter(e => {
                if (e.type === 'wheel') return e.ctrlKey;
                if (e.type.startsWith('touch')) return e.touches.length > 1;
                return !e.button;
            })
            .on('zoom', e => render(e.transform));
        svg.call(zoom).on('dblclick.zoom', null);
        render(d3.zoomIdentity);

        // 확대 버튼
        const ctrl = d3.select(el).append('div').attr('class', 'jmap-ctrl');
        ctrl.append('button').attr('type', 'button').attr('aria-label', '확대').text('+')
            .on('click', () => svg.transition().duration(250).call(zoom.scaleBy, 1.6));
        ctrl.append('button').attr('type', 'button').attr('aria-label', '축소').text('−')
            .on('click', () => svg.transition().duration(250).call(zoom.scaleBy, 1 / 1.6));
        ctrl.append('button').attr('type', 'button').attr('aria-label', '처음 위치').text('⟲')
            .on('click', () => svg.transition().duration(300).call(zoom.transform, d3.zoomIdentity));

        // 출처 표기
        d3.select(el).append('div').attr('class', 'jmap-credit').text('지도 데이터 © Natural Earth');
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
