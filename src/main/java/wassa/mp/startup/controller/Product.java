package wassa.mp.startup.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class Product {
    public record  produit(int id,String nom,double price){};
    public List<produit>produits= new  ArrayList<>(
            List.of(
                     new produit(1,"mangue",45),
                    new produit(2,"tomate",5),
                    new produit(3,"goyabe",4)

            )

    );
    @GetMapping
    public  List<produit>getProduits(){
        return produits;

    }
    @PostMapping
    public produit addProduct(@RequestBody produit produitss){
           produits.add(produitss)   ;
           return  produitss;
    }
}
