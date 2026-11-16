/*
 * 소속이 SL·ED 면 조 선택을 숨기고 'SLED조로 자동 배정' 안내를 보여준다.
 *  <select id="teamNo" data-sled-from="community">  : 소속 select 의 id
 *  소속 option 에는 data-sled="true|false" (Community enum 의 sled 값)
 *  실제 배정은 서버(MemberService.resolveTeam)가 한다. 여기서는 화면만 바꾼다.
 */
(function () {
    'use strict';
    document.querySelectorAll('select[data-sled-from]').forEach(team => {
        const community = document.getElementById(team.dataset.sledFrom);
        const note = team.parentElement.querySelector('.sled-note');
        if (!community) return;
        const sync = () => {
            const opt = community.selectedOptions[0];
            const sled = !!opt && opt.dataset.sled === 'true';
            team.hidden = sled;
            team.disabled = sled;
            if (note) note.hidden = !sled;
        };
        community.addEventListener('change', sync);
        sync();
    });
})();
