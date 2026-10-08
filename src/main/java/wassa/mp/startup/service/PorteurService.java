package wassa.mp.startup.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import wassa.mp.startup.Enumeration.RoleEnum;
import wassa.mp.startup.dto.PorteurRequestDto;
import wassa.mp.startup.dto.PorteurResponseDto;
import wassa.mp.startup.model.Porteur;
import wassa.mp.startup.repository.PorteurRepository;

import java.time.LocalDate;

@Service
public class PorteurService {
    private final PorteurRepository porteurRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    //private final PorteurMapper porteurMapper;
    public PorteurService(PorteurRepository porteurRepository,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.porteurRepository = porteurRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        //this.porteurMapper = porteurMapper;
    }
    //l'incription d'un porteur de projet
    public PorteurResponseDto registerPorteur(PorteurRequestDto porteurRequestDto){
        Porteur porteur = new Porteur();
        porteur.setNom(porteurRequestDto.getNom());
        porteur.setPrenom(porteurRequestDto.getPrenom());
        porteur.setEmail(porteurRequestDto.getEmail());
        porteur.setPassword(bCryptPasswordEncoder.encode(porteurRequestDto.getPassword()));
        porteur.setTelephone(porteurRequestDto.getTelephone());
        porteur.setDateCreation(LocalDate.now());
        porteur.setRole(RoleEnum.PORTEUR);
        Porteur pourAjouter = porteurRepository.save(porteur);
        return new PorteurResponseDto(pourAjouter.getId(),pourAjouter.getNom(),pourAjouter.getPrenom(),pourAjouter.getTelephone(),pourAjouter.getEmail());
    }



}
