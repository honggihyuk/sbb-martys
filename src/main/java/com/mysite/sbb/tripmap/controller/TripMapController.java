package com.mysite.sbb.tripmap.controller;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.service.MemberService;
import com.mysite.sbb.study.dto.BiblePlaces;
import com.mysite.sbb.tripmap.dto.TripDayDto;
import com.mysite.sbb.tripmap.dto.TripMapData;
import com.mysite.sbb.tripmap.dto.TripStopDto;
import com.mysite.sbb.tripmap.entity.TripDay;
import com.mysite.sbb.tripmap.entity.TripStop;
import com.mysite.sbb.tripmap.service.TripMapService;
import com.mysite.sbb.tripmap.weather.WeatherInfo;
import com.mysite.sbb.tripmap.weather.WeatherService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

// 지도 : Google Maps 위에 답사 일정을 날짜별로 계획하고, 다녀온 곳을 기록한다
//  - 누구나 : 일정·장소 추가, '다녀왔어요' 체크
//  - 만든 사람 / 조장 / 관리자 : 고치기, 순서 바꾸기, 지우기
@Controller
@RequiredArgsConstructor
@RequestMapping("/map")
public class TripMapController {

    private final TripMapService tripMapService;
    private final MemberService memberService;
    private final WeatherService weatherService;

    // application.yml 의 app.google-maps.* (키는 환경변수 GOOGLE_MAPS_API_KEY 로 넣는다)
    @Value("${app.google-maps.api-key:}")
    private String apiKey;

    @Value("${app.google-maps.map-id:DEMO_MAP_ID}")
    private String mapId;

    @GetMapping
    public String map(@RequestParam(value = "day", required = false) Integer dayNo,
                      TripDayDto tripDayDto, Principal principal, Model model) {
        List<TripDay> days = tripMapService.getDays();
        TripDay selected = (dayNo == null) ? null
                : days.stream().filter(d -> d.getDayNo().equals(dayNo)).findFirst().orElse(null);

        if (tripDayDto.getDayNo() == null) {
            tripDayDto.setDayNo(tripMapService.nextDayNo());
        }
        model.addAttribute("days", days);
        model.addAttribute("stopCount", days.stream().mapToInt(d -> d.getStops().size()).sum());
        model.addAttribute("selected", selected);
        model.addAttribute("shownDays", selected == null ? days : List.of(selected));
        model.addAttribute("me", memberService.getMember(principal.getName()));
        model.addAttribute("departure", TripInfo.DEPARTURE);
        model.addAttribute("biblePlaces", BiblePlaces.list());
        model.addAttribute("apiKey", apiKey);
        model.addAttribute("mapId", mapId);
        return "tripmap/map";
    }

    // 지도에 그릴 데이터 (JSON)
    @GetMapping("/data")
    @ResponseBody
    public TripMapData data() {
        return TripMapData.of(tripMapService.getDays());
    }

    // 장소 날씨 (JSON) : 지금 날씨 + 일정 날 날씨. 못 가져오면 204 (내용 없음)
    @GetMapping("/weather")
    @ResponseBody
    public ResponseEntity<WeatherInfo> weather(@RequestParam("lat") double lat,
                                               @RequestParam("lng") double lng,
                                               @RequestParam("date") LocalDate date) {
        WeatherInfo info = weatherService.getWeather(lat, lng, date);
        return info == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(info);
    }

    // ---------- 하루 일정 ----------

    @PostMapping("/day")
    public String createDay(@Valid TripDayDto tripDayDto, BindingResult bindingResult,
                            Principal principal, RedirectAttributes ra) {
        if (!bindingResult.hasErrors() && tripMapService.existsDayNo(tripDayDto.getDayNo(), null)) {
            bindingResult.rejectValue("dayNo", "duplicate", tripDayDto.getDayNo() + "일차는 이미 있어요.");
        }
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/map";
        }
        TripDay day = tripMapService.createDay(tripDayDto, memberService.getMember(principal.getName()));
        return "redirect:/map?day=" + day.getDayNo();
    }

    @PostMapping("/day/{id}/edit")
    public String editDay(@PathVariable("id") Long id, @Valid TripDayDto tripDayDto, BindingResult bindingResult,
                          Principal principal, RedirectAttributes ra) {
        TripDay day = tripMapService.getDay(id);
        if (!tripMapService.canEdit(memberService.getMember(principal.getName()), day.getAuthor())) {
            return "redirect:/denied";
        }
        if (!bindingResult.hasErrors() && tripMapService.existsDayNo(tripDayDto.getDayNo(), id)) {
            bindingResult.rejectValue("dayNo", "duplicate", tripDayDto.getDayNo() + "일차는 이미 있어요.");
        }
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/map?day=" + day.getDayNo();
        }
        tripMapService.updateDay(id, tripDayDto);
        return "redirect:/map?day=" + tripDayDto.getDayNo();
    }

    @PostMapping("/day/{id}/delete")
    public String deleteDay(@PathVariable("id") Long id, Principal principal) {
        TripDay day = tripMapService.getDay(id);
        if (!tripMapService.canEdit(memberService.getMember(principal.getName()), day.getAuthor())) {
            return "redirect:/denied";
        }
        tripMapService.deleteDay(id);
        return "redirect:/map";
    }

    // ---------- 장소 ----------

    @PostMapping("/stop")
    public String addStop(@Valid TripStopDto tripStopDto, BindingResult bindingResult,
                          Principal principal, RedirectAttributes ra) {
        TripDay day = tripMapService.getDay(tripStopDto.getDayId());
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
        } else {
            tripMapService.addStop(tripStopDto, memberService.getMember(principal.getName()));
        }
        return "redirect:/map?day=" + day.getDayNo();
    }

    @PostMapping("/stop/{id}/move")
    public String move(@PathVariable("id") Long id, @RequestParam("dir") String dir, Principal principal) {
        TripStop stop = tripMapService.getStop(id);
        if (!tripMapService.canEdit(memberService.getMember(principal.getName()), stop.getAuthor())) {
            return "redirect:/denied";
        }
        tripMapService.move(id, "up".equals(dir));
        return "redirect:/map?day=" + stop.getDay().getDayNo() + "#stop-" + id;
    }

    @PostMapping("/stop/{id}/visited")
    public String visited(@PathVariable("id") Long id) {
        TripStop stop = tripMapService.getStop(id);
        tripMapService.toggleVisited(id);
        return "redirect:/map?day=" + stop.getDay().getDayNo() + "#stop-" + id;
    }

    @PostMapping("/stop/{id}/edit")
    public String editStop(@PathVariable("id") Long id,
                           @RequestParam(value = "time", required = false) String time,
                           @RequestParam(value = "memo", required = false) String memo,
                           Principal principal) {
        TripStop stop = tripMapService.getStop(id);
        if (!tripMapService.canEdit(memberService.getMember(principal.getName()), stop.getAuthor())) {
            return "redirect:/denied";
        }
        tripMapService.updateStop(id, time, memo);
        return "redirect:/map?day=" + stop.getDay().getDayNo() + "#stop-" + id;
    }

    @PostMapping("/stop/{id}/delete")
    public String deleteStop(@PathVariable("id") Long id, Principal principal) {
        TripStop stop = tripMapService.getStop(id);
        int dayNo = stop.getDay().getDayNo();
        if (!tripMapService.canEdit(memberService.getMember(principal.getName()), stop.getAuthor())) {
            return "redirect:/denied";
        }
        tripMapService.deleteStop(id);
        return "redirect:/map?day=" + dayNo;
    }
}
