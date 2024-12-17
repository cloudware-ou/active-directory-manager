package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.Command;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@RequiredArgsConstructor
public class NotificationListener {


    @Autowired
    private DataSource dataSource;

    private final CommandService commandService;

    private final ConcurrentHashMap<Long, BlockingQueue<Command>> completedCommandsQueue = new ConcurrentHashMap<>();

    @PostConstruct
    public void startListening() {
        new Thread(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.execute("LISTEN completed_commands;");

                PGConnection pgConnection = connection.unwrap(PGConnection.class);

                while (!Thread.currentThread().isInterrupted()) {
                    // Wait for notifications
                    PGNotification[] nts = pgConnection.getNotifications(0);
                    if (nts == null) {
                        continue;
                    }
                    for (PGNotification nt : nts) {
                        Long id = Long.valueOf(nt.getParameter());
                        Command command = commandService.getCommand(id);
                        getQueue(id).add(command);
                    }
                }
            } catch (SQLException e){
                System.err.println(e.getMessage());
            }
        }).start();
    }

    private BlockingQueue<Command> getQueue(Long key) {
        return completedCommandsQueue.computeIfAbsent(key, k -> new LinkedBlockingQueue<>());
    }

    public Command getCompletedCommand(Long id) throws InterruptedException {
        return getQueue(id).take();
    }
}
