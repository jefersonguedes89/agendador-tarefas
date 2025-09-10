package com.javanauta.agendadortarefas.business;


import com.javanauta.agendadortarefas.business.dto.TarefasDTO;
import com.javanauta.agendadortarefas.business.dto.TarefasDTORecord;
import com.javanauta.agendadortarefas.business.mapper.TarefaUpdateConverter;
import com.javanauta.agendadortarefas.business.mapper.TarefasConverter;
import com.javanauta.agendadortarefas.infrastructure.entity.TarefasEntity;
import com.javanauta.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import com.javanauta.agendadortarefas.infrastructure.exceptions.ResourceNotFoundException;
import com.javanauta.agendadortarefas.infrastructure.repository.TarefasRepository;
import com.javanauta.agendadortarefas.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TarefasService {

    private final TarefasRepository tarefasRepository;
    private final TarefasConverter tarefaConverter;
    private final JwtUtil jwtUtil;
    private final TarefaUpdateConverter tarefaUpdateConverter;

    public TarefasDTORecord gravarTarefa(String token, TarefasDTORecord tarefasDTO) {
        String email = jwtUtil.extractUsername(token.substring(7));

        TarefasDTORecord dtoFinal = new TarefasDTORecord(null, tarefasDTO.nomeTarefa(), tarefasDTO.descricao(),
                LocalDateTime.now(), tarefasDTO.dataEvento(), email, null,StatusNotificacaoEnum.PENDENTE );

//        // horário de criação sempre em UTC
//        tarefasDTO.setDataCriacao(LocalDateTime.now(ZoneOffset.UTC));
//
//        // converte o dataEvento recebido como horário local para UTC
//        if (tarefasDTO.getDataEvento() != null) {
//            LocalDateTime dataEventoUtc = tarefasDTO.getDataEvento()
//                    .atZone(ZoneId.systemDefault())       // interpreta como horário do servidor
//                    .withZoneSameInstant(ZoneOffset.UTC) // converte para UTC
//                    .toLocalDateTime();
//            tarefasDTO.setDataEvento(dataEventoUtc);
//        }
//
//        tarefasDTO.setStatusNotificacaoEnum(StatusNotificacaoEnum.PENDENTE);
//        tarefasDTO.setEmailUsuario(email);

        TarefasEntity tarefasEntity = tarefaConverter.paraTarefaEntity(dtoFinal);

        TarefasEntity saved = tarefasRepository.save(tarefasEntity);
        TarefasDTORecord resultado = tarefaConverter.paraTarefaDTO(saved);

//        log.info("Tarefa gravada - dataEvento (UTC): {}", resultado.getDataEvento());

        return resultado;
    }

    public List<TarefasDTORecord> buscarTarefasAgendadasPorPeriodo(LocalDateTime dataIncial, LocalDateTime dataFinal) {
        log.info(String.valueOf(dataIncial));
        log.info(String.valueOf(dataFinal));
        return tarefaConverter.paraListaTarefasDTORecord(
                tarefasRepository.findByDataEventoBetweenAndStatusNotificacaoEnum(
                        dataIncial, dataFinal, StatusNotificacaoEnum.PENDENTE)
        );
    }

    public List<TarefasDTORecord> buscarTarefasPorEmail(String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        return tarefaConverter.paraListaTarefasDTORecord(tarefasRepository.findByEmailUsuario(email));
    }

    public void deletaTarefaPorId(String id) {
        tarefasRepository.deleteById(id);
    }


    public TarefasDTORecord alteraStatus(StatusNotificacaoEnum statusNotificacaoEnum, String id) {

        try {
            TarefasEntity tarefasEntity = tarefasRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("Tarefa não encontrada " + id)
            );
            tarefasEntity.setStatusNotificacaoEnum(statusNotificacaoEnum);
            return tarefaConverter.paraTarefaDTO(tarefasRepository.save(tarefasEntity));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao alterar status da tarefa" + e.getCause());
        }

    }

    public TarefasDTORecord updateTarefas(TarefasDTORecord dto, String id) {
        try {
            TarefasEntity tarefasEntity = tarefasRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("Tarefa não encontrada " + id)
            );

            tarefaUpdateConverter.updateTarefas(dto, tarefasEntity);

            return tarefaConverter.paraTarefaDTO(tarefasRepository.save(tarefasEntity));

        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao alterar status da tarefa" + e.getCause());
        }
    }

}
