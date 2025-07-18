// src/main/java/com/example/demo/Controller/ArbitroController.java
package com.example.demo.Controller;

import com.example.demo.Model.Partida;
import com.example.demo.Service.ArbitroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Controller.dto.PlacarRequest;


@RestController
@RequestMapping("/api/arbitros")
public class ArbitroController {

    @Autowired
    private ArbitroService arbitroService;

    @PutMapping("/{matriculaArbitro}/partidas/{partidaId}/resultado")
    public ResponseEntity<?> registrarResultado(
            @PathVariable String matriculaArbitro,
            @PathVariable Long partidaId,
            @RequestBody PlacarRequest placar) {

        try {
            Partida partidaAtualizada = arbitroService.registrarResultado(
                    matriculaArbitro,
                    partidaId,
                    placar.getPlacarA(),
                    placar.getPlacarB()
            );
            // Retorna a partida com o resultado atualizado e o status 200 OK
            return new ResponseEntity<>(partidaAtualizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
