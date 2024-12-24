package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.Command;
import com.nortal.activedirectoryrestapi.entities.OneTimeKeys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(NotificationListener.class);
    private final DataSource dataSource;
    private final CommandService commandService;
    private final OneTimeKeysService oneTimeKeysService;

    private final ConcurrentHashMap<Long, BlockingQueue<Command>> completedCommandsQueue = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, BlockingQueue<OneTimeKeys>> oneTimeKeysQueue = new ConcurrentHashMap<>();

    @PostConstruct
    public void startListening() {
        new Thread(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.execute("LISTEN completed_commands;");
                statement.execute("LISTEN one_time_keys_bob");

                PGConnection pgConnection = connection.unwrap(PGConnection.class);

                while (!Thread.currentThread().isInterrupted()) {
                    // Wait for notifications
                    PGNotification[] nts = pgConnection.getNotifications(10000);
                    if (nts == null) {
                        continue;
                    }
                    for (PGNotification nt : nts) {
                        logger.info("Received notification: {}: {}", nt.getName(), nt.getParameter());
                        Long id = Long.valueOf(nt.getParameter());
                        switch (nt.getName()){
                            case "completed_commands":
                                Command command = commandService.getCommand(id);
                                getCompletedCommandsQueue(id).add(command);
                                break;
                            case "one_time_keys_bob":
                                OneTimeKeys oneTimeKeys = oneTimeKeysService.getOneTimeKeys(id);
                                getOneTimeKeysQueue(id).add(oneTimeKeys);
                        }
                    }
                }
            } catch (SQLException e){
                System.err.println(e.getMessage());
            }
        }).start();
    }

    private BlockingQueue<Command> getCompletedCommandsQueue(Long key) {
        return completedCommandsQueue.computeIfAbsent(key, k -> new LinkedBlockingQueue<>());
    }

    private BlockingQueue<OneTimeKeys> getOneTimeKeysQueue(Long key) {
        return oneTimeKeysQueue.computeIfAbsent(key, k -> new LinkedBlockingQueue<>());
    }

    public Command getCompletedCommand(Long id) throws InterruptedException {
        return getCompletedCommandsQueue(id).take();
    }

    public OneTimeKeys getOneTimeKeys(Long id) throws InterruptedException {
        return getOneTimeKeysQueue(id).take();
    }
}
