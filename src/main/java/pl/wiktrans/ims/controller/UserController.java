package pl.wiktrans.ims.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.wiktrans.ims.dto.UserDto;
import pl.wiktrans.ims.util.FailableActionResult;
import pl.wiktrans.ims.util.FailableResource;
import pl.wiktrans.ims.model.User;
import pl.wiktrans.ims.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/all")
    public FailableResource<List<UserDto>> getAll() {

        List<UserDto> users = userService.getAll()
                .stream()
                .map(UserDto::of)
                .collect(Collectors.toList());

        return FailableResource.success(users);
    }

    @GetMapping("{id}")
    public FailableResource<UserDto> getById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);

            return FailableResource.success(UserDto.of(user));
        } catch (Exception e) {
            return FailableResource.failure(e.getMessage());
        }
    }

    @PostMapping
    public FailableActionResult addNewUser(@RequestBody UserDto userDto) {
        try {
            if (userDto.getId() != null) {
                userService.updateUser(userDto);
            } else {
                userService.addNewUser(userDto);
            }
            return FailableActionResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Add/edit user error: ", e.getMessage());
            return FailableActionResult.failure(e.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public FailableActionResult updateUser(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return FailableActionResult.success();
        } catch (Exception e) {
            return FailableActionResult.failure(e.getMessage());
        }
    }
}
