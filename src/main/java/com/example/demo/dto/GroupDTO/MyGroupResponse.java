package com.example.demo.dto.GroupDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyGroupResponse {
    private Long id;
    private String groupName;
    private String avatarUrl;
    private String role;
}
