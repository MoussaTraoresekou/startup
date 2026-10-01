package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/porteur")
@SecurityRequirement(name = "bearerAuth")
public class Product {
    public record  produit(int id,String nom,double price){};
    public List<produit>produits= new  ArrayList<>(
            List.of(
                     new produit(1,"mangue",45),
                    new produit(2,"tomate",5),
                    new produit(3,"goyabe",4)

            )

    );
    @GetMapping("/produits")
    public  List<produit>getProduits(){
        return produits;

    }
    @PostMapping("/produits")
    public produit addProduct(@RequestBody produit produitss){
           produits.add(produitss)   ;
           return  produitss;
    }
}
