package br.com.webbudget.application.components.ui.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
public abstract class BasicFilter implements Serializable {

    @Getter
    @Setter
    protected String value;
}
