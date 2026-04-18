package com.example.demo.dto.GroupDTO;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupProfileUpdate {
    @Size(max = 255, message = "Tên nhóm không được vượt quá 255 ký tự")
    private String groupName;
}
