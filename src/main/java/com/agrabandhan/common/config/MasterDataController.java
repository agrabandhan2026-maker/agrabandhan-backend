package com.agrabandhan.common.config;

import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.common.entity.Gotra;
import com.agrabandhan.profile.entity.*;
import com.agrabandhan.profile.entity.Profile.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/master-data")
@Tag(name = "Master Data", description = "Dropdown values and enum lists for mobile app")
public class MasterDataController {

    @GetMapping("/gotras")
    @Operation(summary = "Get all 18 Bisa Aggarwal gotras")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getGotras() {
        List<Map<String, String>> gotras = Arrays.stream(Gotra.values())
                .map(g -> Map.of("value", g.name(), "label", g.getDisplayName()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(gotras));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all master data in a single call (for mobile app initialization)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllMasterData() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("gotras", enumToList(Gotra.values()));
        data.put("genders", enumToList(Gender.values()));
        data.put("maritalStatuses", enumToList(MaritalStatus.values()));
        data.put("profileManagedBy", enumToList(ProfileManagedBy.values()));
        data.put("familyTypes", enumToList(FamilyDetail.FamilyType.values()));
        data.put("familyValues", enumToList(FamilyDetail.FamilyValues.values()));
        data.put("familyAffluence", enumToList(FamilyDetail.FamilyAffluence.values()));
        data.put("qualifications", enumToList(EducationDetail.Qualification.values()));
        data.put("employedIn", enumToList(ProfessionDetail.EmployedIn.values()));
        data.put("diets", enumToList(LifestyleDetail.Diet.values()));
        data.put("habits", enumToList(LifestyleDetail.Habit.values()));
        data.put("photoVisibility", enumToList(ProfilePhoto.PhotoVisibility.values()));

        data.put("incomeRanges", List.of(
                Map.of("value", "BELOW_2L", "label", "Below ₹2 Lakh"),
                Map.of("value", "2L_5L", "label", "₹2 - 5 Lakh"),
                Map.of("value", "5L_10L", "label", "₹5 - 10 Lakh"),
                Map.of("value", "10L_20L", "label", "₹10 - 20 Lakh"),
                Map.of("value", "20L_50L", "label", "₹20 - 50 Lakh"),
                Map.of("value", "50L_1CR", "label", "₹50 Lakh - 1 Crore"),
                Map.of("value", "ABOVE_1CR", "label", "Above ₹1 Crore"),
                Map.of("value", "NOT_DISCLOSED", "label", "Prefer not to say")
        ));

        data.put("heightRange", Map.of("min", 120, "max", 220, "unit", "cm"));

        data.put("complexions", List.of(
                Map.of("value", "VERY_FAIR", "label", "Very Fair"),
                Map.of("value", "FAIR", "label", "Fair"),
                Map.of("value", "WHEATISH", "label", "Wheatish"),
                Map.of("value", "DARK", "label", "Dark")
        ));

        data.put("bloodGroups", List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));

        data.put("businessTurnoverRanges", List.of(
                Map.of("value", "BELOW_10L", "label", "Below ₹10 Lakh"),
                Map.of("value", "10L_50L", "label", "₹10 - 50 Lakh"),
                Map.of("value", "50L_1CR", "label", "₹50 Lakh - 1 Crore"),
                Map.of("value", "1CR_5CR", "label", "₹1 - 5 Crore"),
                Map.of("value", "5CR_10CR", "label", "₹5 - 10 Crore"),
                Map.of("value", "ABOVE_10CR", "label", "Above ₹10 Crore"),
                Map.of("value", "NOT_DISCLOSED", "label", "Prefer not to say")
        ));

        data.put("indianStates", List.of(
                "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
                "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
                "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
                "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
                "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
                "Uttar Pradesh", "Uttarakhand", "West Bengal",
                "Delhi", "Chandigarh", "Jammu & Kashmir", "Ladakh", "Puducherry"
        ));

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // =============================================
    //  Individual enum endpoints (optional)
    // =============================================

    @GetMapping("/genders")
    @Operation(summary = "Get gender options")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getGenders() {
        return ResponseEntity.ok(ApiResponse.success(enumToList(Gender.values())));
    }

    @GetMapping("/marital-statuses")
    @Operation(summary = "Get marital status options")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getMaritalStatuses() {
        return ResponseEntity.ok(ApiResponse.success(enumToList(MaritalStatus.values())));
    }

    @GetMapping("/qualifications")
    @Operation(summary = "Get qualification options")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getQualifications() {
        return ResponseEntity.ok(ApiResponse.success(enumToList(EducationDetail.Qualification.values())));
    }

    @GetMapping("/employed-in")
    @Operation(summary = "Get employment type options")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getEmployedIn() {
        return ResponseEntity.ok(ApiResponse.success(enumToList(ProfessionDetail.EmployedIn.values())));
    }

    @GetMapping("/diets")
    @Operation(summary = "Get diet options")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getDiets() {
        return ResponseEntity.ok(ApiResponse.success(enumToList(LifestyleDetail.Diet.values())));
    }

    // =============================================
    //  Helper
    // =============================================
    private <E extends Enum<E>> List<Map<String, String>> enumToList(E[] values) {
        return Arrays.stream(values)
                .map(e -> {
                    String label;
                    if (e instanceof Gotra g) {
                        label = g.getDisplayName();
                    } else {
                        // Convert ENUM_VALUE to Title Case: NEVER_MARRIED -> Never Married
                        label = Arrays.stream(e.name().split("_"))
                                .map(w -> w.charAt(0) + w.substring(1).toLowerCase())
                                .collect(Collectors.joining(" "));
                    }
                    return Map.of("value", e.name(), "label", label);
                })
                .toList();
    }
}
