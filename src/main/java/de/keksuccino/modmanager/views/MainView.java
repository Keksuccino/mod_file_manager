package de.keksuccino.modmanager.views;

import de.keksuccino.modmanager.ModManager;
import de.keksuccino.modmanager.UploadHandler;
import de.keksuccino.modmanager.ModFileHandler;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import de.keksuccino.core.TokenExtractor;

public class MainView extends JFrame {

    public MainView() {
        setTitle("Mod Manager v" + ModManager.VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        // --- Check for token extraction errors ---
        boolean tokensMissing = false;
        try {
            TokenExtractor tokenExtractor = new TokenExtractor(ModManager.config.getOrDefault("token_file", ""));
            String curseToken = tokenExtractor.getCurseForgeToken();
            String modrinthToken = tokenExtractor.getModrinthToken();
            if (curseToken == null || curseToken.trim().isEmpty() ||
                    modrinthToken == null || modrinthToken.trim().isEmpty()) {
                tokensMissing = true;
            }
        } catch (Exception e) {
            tokensMissing = true;
        }

        // Create an error panel to display a token error message if necessary.
        JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        if (tokensMissing) {
            JLabel errorLabel = new JLabel("Error: One or both tokens are missing!");
            errorLabel.setForeground(Color.RED);
            errorPanel.add(errorLabel);
        }

        // Create a panel for the buttons.
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create buttons.
        JButton configButton = new JButton("Open Config");
        JButton prepareButton = new JButton("Prepare Mod Files");
        JButton uploadCurseForgeButton = new JButton("Upload to CurseForge");
        JButton uploadModrinthButton = new JButton("Upload to Modrinth");
        JButton uploadBothButton = new JButton("Upload to Modrinth & CurseForge");
        JButton exitButton = new JButton("Exit");

        // If tokens are missing, disable all upload buttons.
        if (tokensMissing) {
            uploadCurseForgeButton.setEnabled(false);
            uploadModrinthButton.setEnabled(false);
            uploadBothButton.setEnabled(false);
        }

        // Create a progress bar.
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        // Action: Open the configuration window.
        configButton.addActionListener(e ->
                SwingUtilities.invokeLater(() -> new ConfigView().setVisible(true))
        );

        // Action: Prepare the mod files with progress.
        prepareButton.addActionListener(e -> {
            prepareButton.setEnabled(false);
            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() {
                    try {
                        publish(0);
                        boolean sourcesCopied = ModFileHandler.copySourcesJarToOut();
                        if (!sourcesCopied) {
                            throw new Exception("Sources jar copy failed.");
                        }
                        publish(50);
                        boolean mainCopied = ModFileHandler.copyMainJarToOut();
                        if (!mainCopied) {
                            throw new Exception("Main jar copy failed.");
                        }
                        publish(100);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
                @Override
                protected void done() {
                    prepareButton.setEnabled(true);
                    try {
                        get();
                        JOptionPane.showMessageDialog(MainView.this,
                                "Mod files prepared successfully!",
                                "Preparation Result", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainView.this,
                                "Preparation failed: " + ex.getCause().getMessage(),
                                "Preparation Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        progressBar.setValue(0);
                    }
                }
            };
            worker.execute();
        });

        // Action: Upload to CurseForge only.
        uploadCurseForgeButton.addActionListener(e -> {
            uploadCurseForgeButton.setEnabled(false);
            uploadModrinthButton.setEnabled(false);
            uploadBothButton.setEnabled(false);

            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish(0);
                    UploadHandler.upload(false, true);
                    publish(100);
                    return null;
                }
                @Override
                protected void process(List<Integer> chunks) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
                @Override
                protected void done() {
                    uploadCurseForgeButton.setEnabled(true);
                    uploadModrinthButton.setEnabled(true);
                    uploadBothButton.setEnabled(true);
                    try {
                        get();
                        JOptionPane.showMessageDialog(MainView.this,
                                "Upload to CurseForge completed!",
                                "Upload", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainView.this,
                                "Upload failed: " + ex.getCause().getMessage(),
                                "Upload Error", JOptionPane.ERROR_MESSAGE);
                    }
                    progressBar.setValue(0);
                }
            };
            worker.execute();
        });

        // Action: Upload to Modrinth only.
        uploadModrinthButton.addActionListener(e -> {
            uploadCurseForgeButton.setEnabled(false);
            uploadModrinthButton.setEnabled(false);
            uploadBothButton.setEnabled(false);

            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish(0);
                    UploadHandler.upload(true, false);
                    publish(100);
                    return null;
                }
                @Override
                protected void process(List<Integer> chunks) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
                @Override
                protected void done() {
                    uploadCurseForgeButton.setEnabled(true);
                    uploadModrinthButton.setEnabled(true);
                    uploadBothButton.setEnabled(true);
                    try {
                        get();
                        JOptionPane.showMessageDialog(MainView.this,
                                "Upload to Modrinth completed!",
                                "Upload", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainView.this,
                                "Upload failed: " + ex.getCause().getMessage(),
                                "Upload Error", JOptionPane.ERROR_MESSAGE);
                    }
                    progressBar.setValue(0);
                }
            };
            worker.execute();
        });

        // Action: Upload to both CurseForge and Modrinth.
        uploadBothButton.addActionListener(e -> {
            uploadCurseForgeButton.setEnabled(false);
            uploadModrinthButton.setEnabled(false);
            uploadBothButton.setEnabled(false);

            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish(0);
                    UploadHandler.upload(false, false);
                    publish(100);
                    return null;
                }
                @Override
                protected void process(List<Integer> chunks) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
                @Override
                protected void done() {
                    uploadCurseForgeButton.setEnabled(true);
                    uploadModrinthButton.setEnabled(true);
                    uploadBothButton.setEnabled(true);
                    try {
                        get();
                        JOptionPane.showMessageDialog(MainView.this,
                                "Upload to CurseForge & Modrinth completed!",
                                "Upload", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainView.this,
                                "Upload failed: " + ex.getCause().getMessage(),
                                "Upload Error", JOptionPane.ERROR_MESSAGE);
                    }
                    progressBar.setValue(0);
                }
            };
            worker.execute();
        });

        // Action: Exit the application.
        exitButton.addActionListener(e -> System.exit(0));

        // Add buttons to the button panel.
        buttonPanel.add(configButton);
        buttonPanel.add(prepareButton);
        buttonPanel.add(uploadCurseForgeButton);
        buttonPanel.add(uploadModrinthButton);
        buttonPanel.add(uploadBothButton);
        buttonPanel.add(exitButton);

        // Create a container panel to hold the error panel, button panel, and progress bar.
        JPanel container = new JPanel(new BorderLayout());
        container.add(errorPanel, BorderLayout.NORTH);
        container.add(buttonPanel, BorderLayout.CENTER);
        container.add(progressBar, BorderLayout.SOUTH);

        add(container, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        ModManager.updateConfig();
        SwingUtilities.invokeLater(() -> new MainView().setVisible(true));
    }

}
