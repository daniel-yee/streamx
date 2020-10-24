package com.streamxhub.console.core.service.impl;

import com.streamxhub.console.core.entity.Note;
import com.streamxhub.console.core.service.NoteBookService;
import com.streamxhub.repl.flink.interpreter.FlinkInterpreter;
import com.streamxhub.repl.flink.interpreter.FlinkShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.zeppelin.display.AngularObjectRegistry;
import org.apache.zeppelin.interpreter.*;
import org.apache.zeppelin.interpreter.remote.RemoteInterpreterEventClient;
import org.springframework.stereotype.Service;

import static org.mockito.Mockito.mock;

import java.util.Properties;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

@Slf4j
@Service("noteBookService")
public class NoteBookServiceImpl implements NoteBookService {

    private FlinkInterpreter interpreter;

    private InterpreterContext getInterpreterContext() throws InterpreterException {
        Properties prop = new Properties();
        prop.setProperty("repl.out", "true");
        prop.setProperty("scala.color", "true");
        prop.setProperty("flink.execution.mode", "application");
        interpreter = new FlinkInterpreter(prop);
        InterpreterGroup interpreterGroup = new InterpreterGroup();
        interpreter.setInterpreterGroup(interpreterGroup);
        interpreter.open();
        AngularObjectRegistry angularObjectRegistry = new AngularObjectRegistry("flink", null);
        InterpreterContext context = InterpreterContext.builder()
                .setParagraphId("paragraphId")
                .setInterpreterOut(new InterpreterOutput(null))
                .setAngularObjectRegistry(angularObjectRegistry)
                .setIntpEventClient(mock(RemoteInterpreterEventClient.class))
                .build();
        InterpreterContext.set(context);
        return context;
    }

    @Override
    public void submit(Note note) throws Exception {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                InterpreterContext context = getInterpreterContext();
                InterpreterResult result = interpreter.interpret(note.getSourceCode(), context);
                System.out.println(context.out.toString());
                assertEquals(InterpreterResult.Code.SUCCESS, result.code());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                if (interpreter != null) {
                    try {
                        interpreter.close();
                    } catch (InterpreterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void submit2(Note note) {

    }
}
