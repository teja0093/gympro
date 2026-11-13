package com.gympro.member_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private long totalMembers;
    private long activeSubscriptions;
    private long expiredSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private List<ExpiringSoon> expiringSoon;

    @Data
    @AllArgsConstructor
    public static class ExpiringSoon {
        private Long memberId;
        private String memberName;
        private String planName;
        private String endDate;
    }
}