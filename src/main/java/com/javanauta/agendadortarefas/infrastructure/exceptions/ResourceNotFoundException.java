package com.javanauta.agendadortarefas.infrastructure.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String mensangem){
        super(mensangem);
    }

    public ResourceNotFoundException(String mensangem, Throwable throwable){
        super(mensangem, throwable);
    }

}
