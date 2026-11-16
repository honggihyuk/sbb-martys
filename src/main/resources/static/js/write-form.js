/*
 * 글쓰기 화면 도우미 (공지사항, 모임 기록, 회의록)
 *  1. 체크박스로 영역 펼치기     : <input type="checkbox" data-toggle-target="아이디">
 *  2. 투표 항목 추가             : <button data-add-option>
 *  3. 댓글 입력하면 '등록' 켜기  : .cmt-input / .cmt-submit
 *  4. 사진 여러 장 미리보기      : .dropzone 안의 <input type="file" multiple>
 *  5. 글 서식 도구               : <button data-md="h1|h2|p|bold|ul|ol" data-for="textarea 아이디">
 *  6. 기본 틀 넣기               : <button data-template="textarea 아이디"> (회의록)
 */
(function () {
    'use strict';

    // 1. 체크하면 펼치기
    document.querySelectorAll('[data-toggle-target]').forEach(box => {
        const target = document.getElementById(box.dataset.toggleTarget);
        const sync = () => target && target.classList.toggle('open', box.checked);
        box.addEventListener('change', sync);
        sync();
    });

    // 2. 투표 선택지 추가 (최대 8개) : 흰 카드 하나 = 선택지 이름 + 설명
    document.querySelectorAll('[data-add-option]').forEach(btn => {
        btn.addEventListener('click', () => {
            const list = btn.parentElement.querySelector('.poll-inputs');
            const count = list.querySelectorAll('.poll-card').length;
            if (count >= 8) return;
            const card = document.createElement('div');
            card.className = 'poll-card';
            const text = document.createElement('input');
            text.type = 'text';
            text.name = 'pollOptions';
            text.placeholder = '선택지 ' + (count + 1);
            text.maxLength = 100;
            const desc = document.createElement('input');
            desc.type = 'text';
            desc.name = 'pollDescriptions';
            desc.placeholder = '설명 (선택) — 예: 이름에 담긴 뜻';
            desc.maxLength = 200;
            card.append(text, desc);
            list.appendChild(card);
            text.focus();
            if (count + 1 >= 8) btn.hidden = true;
        });
    });

    // 6. 기본 틀 넣기 : <button data-template="textarea 아이디"> + <template id="아이디-template">
    //    비어 있으면 그대로 넣고, 이미 글이 있으면 맨 아래에 이어 붙인다
    document.querySelectorAll('[data-template]').forEach(btn => {
        btn.addEventListener('click', () => {
            const ta = document.getElementById(btn.dataset.template);
            const tpl = document.getElementById(btn.dataset.template + '-template');
            const text = tpl.content.textContent.replace(/^\n/, '');
            ta.value = ta.value.trim() === '' ? text : ta.value.replace(/\s*$/, '') + '\n\n' + text;
            ta.focus();
            ta.setSelectionRange(ta.value.length, ta.value.length);
        });
    });

    // 3. 댓글 : 글자를 써야 '등록' 버튼이 켜진다
    document.querySelectorAll('.cmt-form').forEach(form => {
        const input = form.querySelector('.cmt-input');
        const button = form.querySelector('.cmt-submit');
        input.addEventListener('input', () => button.disabled = input.value.trim() === '');
    });

    // 4. 사진 여러 장 : 고른 사진을 작게 보여주고, x 로 뺄 수 있다
    document.querySelectorAll('.dropzone').forEach(zone => {
        const input = zone.querySelector('input[type=file]');
        const previews = document.getElementById(zone.dataset.previews);
        const max = Number(input.dataset.max || 10);
        let files = [];

        function render() {
            // input 의 파일 목록을 지금 고른 사진들로 바꾼다 (폼을 보낼 때 이 목록이 올라간다)
            const dt = new DataTransfer();
            files.forEach(f => dt.items.add(f));
            input.files = dt.files;

            previews.innerHTML = '';
            files.forEach((f, i) => {
                const item = document.createElement('div');
                item.className = 'thumb';
                const img = document.createElement('img');
                img.src = URL.createObjectURL(f);
                img.alt = '';
                const del = document.createElement('button');
                del.type = 'button';
                del.textContent = '×';
                del.setAttribute('aria-label', '사진 빼기');
                del.addEventListener('click', () => { files.splice(i, 1); render(); });
                if (i === 0) {
                    const cover = document.createElement('span');
                    cover.textContent = '대표';
                    item.appendChild(cover);
                }
                item.append(img, del);
                previews.appendChild(item);
            });
            zone.classList.toggle('has-files', files.length > 0);
        }

        function add(list) {
            for (const f of list) {
                if (f.type.startsWith('image/') && files.length < max) files.push(f);
            }
            render();
        }

        input.addEventListener('change', () => add(Array.from(input.files)));
        ['dragenter', 'dragover'].forEach(t => zone.addEventListener(t, e => { e.preventDefault(); zone.classList.add('drag'); }));
        ['dragleave', 'drop'].forEach(t => zone.addEventListener(t, e => { e.preventDefault(); zone.classList.remove('drag'); }));
        zone.addEventListener('drop', e => add(Array.from(e.dataTransfer.files)));
    });

    // 5. 글 서식 도구 : 선택한 줄 앞에 #, ##, -, 1. 을 붙이거나 **굵게** 감싼다
    //    저장된 글은 서버(SimpleMarkdown)가 제목·목록·굵게로 바꿔서 보여준다.
    document.querySelectorAll('[data-md]').forEach(btn => {
        btn.addEventListener('click', () => {
            const ta = document.getElementById(btn.dataset.for);
            const v = ta.value;
            let s = ta.selectionStart, e = ta.selectionEnd;

            if (btn.dataset.md === 'bold') {
                const sel = v.slice(s, e) || '굵은 글씨';
                ta.value = v.slice(0, s) + '**' + sel + '**' + v.slice(e);
                ta.setSelectionRange(s + 2, s + 2 + sel.length);
                ta.focus();
                return;
            }
            // 선택한 줄 전체
            const lineStart = v.lastIndexOf('\n', s - 1) + 1;
            let lineEnd = v.indexOf('\n', e);
            if (lineEnd === -1) lineEnd = v.length;
            const lines = v.slice(lineStart, lineEnd).split('\n').map((line, i) => {
                const plain = line.replace(/^(#{1,2} |- |\d+\. )/, '');
                switch (btn.dataset.md) {
                    case 'h1': return '# ' + plain;
                    case 'h2': return '## ' + plain;
                    case 'ul': return '- ' + plain;
                    case 'ol': return (i + 1) + '. ' + plain;
                    default: return plain;      // 본문
                }
            });
            const text = lines.join('\n');
            ta.value = v.slice(0, lineStart) + text + v.slice(lineEnd);
            ta.setSelectionRange(lineStart, lineStart + text.length);
            ta.focus();
        });
    });
})();
