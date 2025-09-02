package com.javanauta.agendadortarefas.business;


import com.javanauta.agendadortarefas.business.dto.TarefasDTO;
import com.javanauta.agendadortarefas.business.mapper.TarefaUpdateConverter;
import com.javanauta.agendadortarefas.business.mapper.TarefasConverter;
import com.javanauta.agendadortarefas.infrastructure.entity.TarefasEntity;
import com.javanauta.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import com.javanauta.agendadortarefas.infrastructure.exceptions.ResourceNotFoundException;
import com.javanauta.agendadortarefas.infrastructure.repository.TarefasRepository;
import com.javanauta.agendadortarefas.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefasService {

    private final TarefasRepository tarefasRepository;
    private final TarefasConverter tarefaConverter;
    private final JwtUtil jwtUtil;
    private final TarefaUpdateConverter tarefaUpdateConverter;

    public TarefasDTO gravarTarefa(String token, TarefasDTO tarefasDTO){
        String email = jwtUtil.extractUsername(token.substring(7));


        tarefasDTO.setDataCriacao(LocalDateTime.now());
        tarefasDTO.setStatusNotificacaoEnum(StatusNotificacaoEnum.PENDENTE);
        tarefasDTO.setEmailUsuario(email);


        TarefasEntity tarefasEntity = tarefaConverter.paraTarefaEntity(tarefasDTO);

        return tarefaConverter.paraTarefaDTO(tarefasRepository.save(tarefasEntity));
    }

    public List<TarefasDTO> buscarTarefasAgendadasPorPeriodo(LocalDateTime dataIncial, LocalDateTime dataFinal){
        return tarefaConverter.paraListaTarefasDTO(tarefasRepository.findByDataEventoBetween(dataIncial, dataFinal));
    }

    public List<TarefasDTO> buscarTarefasPorEmail(String token){
        String email = jwtUtil.extractUsername(token.substring(7));
        return tarefaConverter.paraListaTarefasDTO(tarefasRepository.findByEmailUsuario(email));
    }

    public void deletaTarefaPorId(String id){
        tarefasRepository.deleteById(id);
    }


    public TarefasDTO alteraStatus(StatusNotificacaoEnum statusNotificacaoEnum,  String id){

        try{
            TarefasEntity tarefasEntity = tarefasRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("Tarefa não encontrada " + id)
            );
            tarefasEntity.setStatusNotificacaoEnum(statusNotificacaoEnum);
            return tarefaConverter.paraTarefaDTO(tarefasRepository.save(tarefasEntity));
        } catch (ResourceNotFoundException e) {
            throw  new ResourceNotFoundException("Erro ao alterar status da tarefa" + e.getCause());
        }

    }

    public TarefasDTO updateTarefas(TarefasDTO dto, String id){
        try{
            TarefasEntity tarefasEntity = tarefasRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("Tarefa não encontrada " + id)
            );

            tarefaUpdateConverter.updateTarefas(dto, tarefasEntity);

           return tarefaConverter.paraTarefaDTO(tarefasRepository.save(tarefasEntity));

        } catch (ResourceNotFoundException e) {
            throw  new ResourceNotFoundException("Erro ao alterar status da tarefa" + e.getCause());
        }
    }

}
