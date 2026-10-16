package com.example.membership.controller;

import com.example.membership.entity.Membership;
import com.example.membership.service.CoinService;
import com.example.membership.service.MembershipService;
import com.example.membership.service.PrivilegeService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MembershipDemoController {

    private final MembershipService membershipService;
    private final PrivilegeService privilegeService;
    private final CoinService coinService;

    // ---------- 会员 ----------

    @PostMapping("/membership/open")
    public Map<String, Object> openMembership(@RequestParam Long userId,
                                              @RequestParam String type,
                                              @RequestParam long durationSeconds,
                                              @RequestParam String originalId) {
        Membership m = membershipService.openMembership(userId, type, durationSeconds, originalId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("userId", m.getUserId());
        res.put("membershipType", m.getMembershipType());
        res.put("beginTime", m.getBeginTime());
        res.put("endTime", m.getEndTime());
        res.put("expired", m.expired());
        return res;
    }

    @GetMapping("/membership/query")
    public Map<String, Object> queryMembership(@RequestParam Long userId, @RequestParam String type) {
        Membership m = membershipService.queryMembership(userId, type);
        Map<String, Object> res = new LinkedHashMap<>();
        if (m == null) {
            res.put("hasMembership", false);
            return res;
        }
        res.put("hasMembership", true);
        res.put("beginTime", m.getBeginTime());
        res.put("endTime", m.getEndTime());
        res.put("expired", m.expired());
        return res;
    }

    // ---------- 道具/特权 ----------

    @PostMapping("/privilege/add")
    public Map<String, Object> addProp(@RequestParam Long userId,
                                       @RequestParam String propType,
                                       @RequestParam long number) {
        return Map.of("left", privilegeService.addProp(userId, propType, number));
    }

    @PostMapping("/privilege/consume")
    public Map<String, Object> consumeProp(@RequestParam Long userId,
                                           @RequestParam String propType,
                                           @RequestParam long number) {
        return Map.of("used", privilegeService.consumePrivilege(userId, propType, number));
    }

    @GetMapping("/privilege/check")
    public Map<String, Object> checkPrivilege(@RequestParam Long userId, @RequestParam String propType) {
        return Map.of("available", privilegeService.checkPrivilege(userId, propType));
    }

    // ---------- 探币 ----------

    @PostMapping("/coin/add")
    public Map<String, Object> addCoin(@RequestParam Long userId,
                                       @RequestParam long amount,
                                       @RequestParam String eventId) {
        return Map.of("balance", coinService.addCoin(eventId, userId, amount));
    }

    @PostMapping("/coin/deduct")
    public Map<String, Object> deductCoin(@RequestParam Long userId,
                                          @RequestParam long amount,
                                          @RequestParam String eventId) {
        return Map.of("balance", coinService.deductCoin(eventId, userId, amount));
    }

    @GetMapping("/coin/balance")
    public Map<String, Object> balance(@RequestParam Long userId) {
        return Map.of("balance", coinService.balanceOf(userId));
    }
}
