package br.ufpr.dinf.gres.opla.view.log;

import br.ufpr.dinf.gres.loglog.Listener;
import br.ufpr.dinf.gres.loglog.LogLogData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextArea;

/**
 *
 * @author elf
 */
public class LogListener implements Listener {

    private JTextArea textArea;

    public LogListener(JTextArea logs) {
        this.textArea = logs;
    }

    @Override
    public void message() {
        this.textArea.append(String.format("%s - %s \n", format(LocalDateTime.now()), LogLogData.printLog()));
    }

    private String format(LocalDateTime dateTime) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(dateTime);
    }
}
