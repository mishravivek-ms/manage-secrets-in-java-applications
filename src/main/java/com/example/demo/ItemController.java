package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;

@RestController
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    @ResponseBody
    @GetMapping(value = "/")
    String welcome() {
        System.out.println("TEST");
        String response=itemRepository.findAll().toString();
        return response;
    }
}
