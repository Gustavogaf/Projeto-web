// src/main/java/com/example/demo/Service/TecnicoService.java
package com.example.demo.Service;

import com.example.demo.Model.*;
import com.example.demo.Repository.CursoRepository;
import com.example.demo.Repository.EquipeRepository;
import com.example.demo.Repository.EsporteRepository;
import com.example.demo.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TecnicoService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EquipeRepository equipeRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private EsporteRepository esporteRepository;

    // --- MÉTODO CORRIGIDO E REESCRITO ---
    @Transactional // Garante que todas as operações de banco de dados aconteçam em uma única transação
    public Equipe cadastrarEquipe(String matriculaTecnico, Equipe equipeInfoRequest, List<String> matriculasAtletas) throws Exception {

        // 1. Validar o Técnico
        Tecnico tecnico = (Tecnico) usuarioRepository.findById(matriculaTecnico)
                .filter(u -> u.getTipo() == TipoUsuario.TECNICO)
                .orElseThrow(() -> new Exception("Técnico com a matrícula " + matriculaTecnico + " não encontrado ou não é um técnico."));

        // 2. Validar e carregar as entidades dependentes (Curso e Esporte)
        if (equipeInfoRequest.getCurso() == null || equipeInfoRequest.getCurso().getId() == null) {
            throw new Exception("O ID do curso é obrigatório.");
        }
        if (equipeInfoRequest.getEsporte() == null || equipeInfoRequest.getEsporte().getId() == null) {
            throw new Exception("O ID do esporte é obrigatório.");
        }

        Curso curso = cursoRepository.findById(equipeInfoRequest.getCurso().getId())
                .orElseThrow(() -> new Exception("Curso com o ID " + equipeInfoRequest.getCurso().getId() + " não encontrado."));

        Esporte esporte = esporteRepository.findById(equipeInfoRequest.getEsporte().getId())
                .orElseThrow(() -> new Exception("Esporte com o ID " + equipeInfoRequest.getEsporte().getId() + " não encontrado."));

        // 3. Validar Regras de Negócio com os dados completos
        if (equipeRepository.existsByCursoAndEsporte(curso, esporte)) {
            throw new Exception("O curso '" + curso.getNome() + "' já possui uma equipe de '" + esporte.getNome() + "'.");
        }
        if (matriculasAtletas.size() < esporte.getMinAtletas() || matriculasAtletas.size() > esporte.getMaxAtletas()) {
            throw new Exception("A quantidade de atletas (" + matriculasAtletas.size() + ") para " + esporte.getNome() +
                    " deve ser entre " + esporte.getMinAtletas() + " e " + esporte.getMaxAtletas() + ".");
        }

        // 4. Criar e Salvar a nova Equipe primeiro para obter um ID
        Equipe novaEquipe = new Equipe();
        novaEquipe.setNome(equipeInfoRequest.getNome());
        novaEquipe.setTecnico(tecnico);
        novaEquipe.setCurso(curso);
        novaEquipe.setEsporte(esporte);
        Equipe equipeSalva = equipeRepository.save(novaEquipe);

        // 5. Buscar, validar e associar os Atletas à equipe já salva
        if (!matriculasAtletas.isEmpty()) {
            List<Atleta> atletasParaSalvar = new ArrayList<>();
            for (String matriculaAtleta : matriculasAtletas) {
                Atleta atleta = (Atleta) usuarioRepository.findById(matriculaAtleta)
                        .filter(u -> u.getTipo() == TipoUsuario.ATLETA)
                        .orElseThrow(() -> new Exception("Atleta com a matrícula '" + matriculaAtleta + "' não encontrado ou não é um atleta."));
                
                atleta.setEquipe(equipeSalva);
                atletasParaSalvar.add(atleta);
            }
            usuarioRepository.saveAll(atletasParaSalvar);
        }
        
        // Retorna a equipe salva, agora com todas as associações corretas.
        return equipeRepository.findById(equipeSalva.getId()).get();
    }
    
    // ... (demais métodos do serviço, como cadastrarAtleta e listarTodos)
    
    public Atleta cadastrarAtleta(String matriculaTecnico, Atleta novoAtleta) throws Exception {
        if (!usuarioRepository.existsById(matriculaTecnico) || usuarioRepository.findById(matriculaTecnico).get().getTipo() != TipoUsuario.TECNICO) {
            throw new Exception("Apenas usuários do tipo TECNICO podem cadastrar atletas.");
        }
        if (usuarioRepository.existsById(novoAtleta.getMatricula())) {
            throw new Exception("Já existe um usuário cadastrado com a matrícula: " + novoAtleta.getMatricula());
        }
        novoAtleta.setTipo(TipoUsuario.ATLETA);
        return usuarioRepository.save(novoAtleta);
    }
    
    public List<Usuario> listarTodos() {
        return usuarioRepository.findByTipo(TipoUsuario.TECNICO);
    }
}