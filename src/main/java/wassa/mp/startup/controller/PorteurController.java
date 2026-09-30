package wassa.mp.startup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wassa.mp.startup.dto.PorteurRequestDto;
import wassa.mp.startup.dto.PorteurResponseDto;
import wassa.mp.startup.service.PorteurService;

@RestController
@RequestMapping("/porteur")
public class PorteurController {
    @Autowired
    private PorteurService  porteurService;
    @PostMapping("/register")
    public PorteurResponseDto createPorteur(@RequestBody PorteurRequestDto porteurRequestDto) {
        System.out.println(porteurRequestDto.getEmail());
         return porteurService.registerPorteur(porteurRequestDto);
    }
}
