package br.ufpr.inf.opla.patterns.models.ps;

import java.util.List;

import arquitetura.representation.Element;
import br.ufpr.inf.opla.patterns.designpatterns.DesignPattern;

public interface PS {

    public DesignPattern getPSOf();

    public boolean isPSOf(DesignPattern designPattern);

    public List<Element> getParticipants();

}
