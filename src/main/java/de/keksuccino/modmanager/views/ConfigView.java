package de.keksuccino.modmanager.views;

import de.keksuccino.modmanager.ModManager;
import de.keksuccino.core.config.Config;
import de.keksuccino.curse.CurseForge;
import de.keksuccino.curse.CurseForge.CurseGameVersion;
import de.keksuccino.core.ModVersionExtractor;
import de.keksuccino.core.TokenExtractor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigView extends JFrame {

    // General tab fields
    private JTextField modJarNameField;
    private JTextField sourcesJarNameField;
    private JTextField renameJarToField;
    private JTextField devJarPrefixField;
    private JTextField buildClassesDirField;
    private JTextField srcDirField;
    private JTextField buildJarDirField;
    private JTextField outputDirField;

    // New fields for displaying the mod version and tokens.
    private JTextField modVersionField;
    private JTextField curseTokenField;
    private JTextField modrinthTokenField;

    // Moved CurseForge settings into General tab:
    private JTextField curseDisplayNameField;
    private JTextField uploadFromFolderField;
    private JTextField modRelationsField;
    // Game Versions checklist.
    private DefaultListModel<CheckListItem> gameVersionsModel;
    private JList<CheckListItem> gameVersionsList;
    private JButton refreshVersionsButton;

    private JComboBox<String> releaseTypeCombo;
    private JComboBox<String> changelogTypeCombo;
    // Changelog text area.
    private JTextArea changelogTextArea;
    private JScrollPane changelogScrollPane;
    private JTextField devFileDescriptionField;

    // CurseForge tab fields
    private JTextField curseEndpointField;
    private JTextField curseProjectIdField;

    // Modrinth tab fields
    private JTextField modrinthProjectIdField;
    private JCheckBox modrinthFeaturedCheck;

    private final Config config;

    // New fields for game versions handling
    private boolean gameVersionsLoaded = false;
    private JButton saveButton; // now an instance field

    public ConfigView() {
        // Use existing configuration from ModManager.
        this.config = ModManager.config;
        setTitle("Configuration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 650);
        setLocationRelativeTo(null);

        // Create tabbed pane.
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel generalPanel = createGeneralPanel();
        JPanel cursePanel = createCursePanel();
        JPanel modrinthPanel = createModrinthPanel();
        tabbedPane.addTab("General", generalPanel);
        tabbedPane.addTab("CurseForge", cursePanel);
        tabbedPane.addTab("Modrinth", modrinthPanel);
        add(tabbedPane, BorderLayout.CENTER);

        // Bottom button panel including Save and Cancel buttons.
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save");
        saveButton.setEnabled(false); // disable until game versions finish loading
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveConfig());
        cancelButton.addActionListener(e -> dispose());

        // Call loadGameVersions() now—after saveButton is created.
        loadGameVersions();
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initialize text fields with config values.
        modJarNameField = new JTextField(config.getOrDefault("mod_jar_name", "modid-1.0.0.jar"));
        sourcesJarNameField = new JTextField(config.getOrDefault("sources_jar_name", "modid-1.0.0-sources.jar"));
        renameJarToField = new JTextField(config.getOrDefault("rename_jar_to", "modid_forge_%version%_MC_1.18-1.18.1.jar"));
        devJarPrefixField = new JTextField(config.getOrDefault("dev_jar_prefix", "sources_"));
        buildClassesDirField = new JTextField(config.getOrDefault("build_classes_dir", "build/classes/java/main"));
        srcDirField = new JTextField(config.getOrDefault("src_dir", "src/main/java"));
        buildJarDirField = new JTextField(config.getOrDefault("build_jar_dir", "build/libs"));
        outputDirField = new JTextField(config.getOrDefault("output_dir", "jar_output"));
        curseDisplayNameField = new JTextField(config.getOrDefault("display_name", "ModId [Forge] v%version% MC 1.18-1.18.1"));
        uploadFromFolderField = new JTextField(config.getOrDefault("upload_from_folder", "jar_output"));
        modRelationsField = new JTextField(config.getOrDefault("mod_relations", "konkrete:requiredDependency,"));

        // Add a row for mod version from ModVersionExtractor.
        modVersionField = new JTextField();
        modVersionField.setEditable(false);
        try {
            String modVersion = ModVersionExtractor.getModVersion();
            modVersionField.setText(modVersion);
        } catch (Exception e) {
            // If gradle.properties is missing, we show an error message.
            modVersionField.setText("Error: Unable to extract mod version");
            modVersionField.setForeground(Color.RED);
        }
        panel.add(createRow("Mod Version:", modVersionField));

        // Add rows for other fields.
        panel.add(createRow("Orig. Main Jar Name:", modJarNameField));
        panel.add(createRow("Orig. Sources Jar Name:", sourcesJarNameField));
        panel.add(createRow("Rename Jar To:", renameJarToField));
        panel.add(createRow("Sources Jar Prefix:", devJarPrefixField));
        panel.add(createRow("Build Jar Dir:", buildJarDirField));
        panel.add(createRow("Output Dir:", outputDirField));
        panel.add(createRow("Display Name:", curseDisplayNameField));
        panel.add(createRow("Upload From Folder:", uploadFromFolderField));
        panel.add(createRow("Mod Relations:", modRelationsField));

        // Game Versions checklist.
        gameVersionsModel = new DefaultListModel<>();
        gameVersionsList = new JList<>(gameVersionsModel);
        gameVersionsList.setCellRenderer(new CheckListRenderer());
        gameVersionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = gameVersionsList.locationToIndex(e.getPoint());
                if (index != -1) {
                    CheckListItem item = gameVersionsModel.getElementAt(index);
                    item.setSelected(!item.isSelected());
                    gameVersionsList.repaint(gameVersionsList.getCellBounds(index, index));
                }
            }
        });
        JScrollPane gameVersionsScroll = new JScrollPane(gameVersionsList);
        gameVersionsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        gameVersionsScroll.setPreferredSize(new Dimension(400, 100));

        refreshVersionsButton = new JButton("Refresh Versions");
        refreshVersionsButton.setPreferredSize(new Dimension(150, 25));
        refreshVersionsButton.setMaximumSize(new Dimension(150, 25));
        refreshVersionsButton.setMinimumSize(new Dimension(150, 25));
        refreshVersionsButton.addActionListener(e -> loadGameVersions());

        JPanel gameVersionsPanel = new JPanel();
        gameVersionsPanel.setLayout(new BoxLayout(gameVersionsPanel, BoxLayout.Y_AXIS));
        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel gameVersionsLabel = new JLabel("Game Versions:");
        gameVersionsLabel.setPreferredSize(new Dimension(150, 25));
        topRow.add(gameVersionsLabel);
        topRow.add(Box.createHorizontalGlue());
        topRow.add(refreshVersionsButton);
        gameVersionsPanel.add(topRow);
        gameVersionsPanel.add(gameVersionsScroll);
        panel.add(gameVersionsPanel);

        releaseTypeCombo = new JComboBox<>(new String[]{"alpha", "beta", "release"});
        releaseTypeCombo.setSelectedItem(config.getOrDefault("release_type", "alpha").toString());
        changelogTypeCombo = new JComboBox<>(new String[]{"text", "html", "markdown"});
        changelogTypeCombo.setSelectedItem(config.getOrDefault("changelog_type", "text").toString());
        String savedChangelog = config.getOrDefault("changelog", "").replace("%n%", "\n");
        changelogTextArea = new JTextArea(savedChangelog, 5, 30);
        changelogTextArea.setLineWrap(true);
        changelogTextArea.setWrapStyleWord(true);
        changelogScrollPane = new JScrollPane(changelogTextArea);
        changelogScrollPane.setPreferredSize(new Dimension(400, 100));
        devFileDescriptionField = new JTextField(config.getOrDefault("dev_file_description", "Deobfuscated dev build."));

        panel.add(createRow("Release Type:", releaseTypeCombo));
        panel.add(createRow("Changelog Type:", changelogTypeCombo));
        panel.add(createTextAreaRow("Changelog:", changelogScrollPane));
        panel.add(createRow("Sources File Description:", devFileDescriptionField));

        return panel;
    }

    private JPanel createCursePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        curseEndpointField = new JTextField(config.getOrDefault("endpoint", "minecraft").toString());
        curseProjectIdField = new JTextField(config.getOrDefault("project_id", -10000L).toString());

        panel.add(createRow("Endpoint:", curseEndpointField));
        panel.add(createRow("Project ID:", curseProjectIdField));

        // Add row for CurseForge token.
        curseTokenField = new JTextField();
        curseTokenField.setEditable(false);
        try {
            TokenExtractor tokenExtractor = new TokenExtractor(ModManager.config.getOrDefault("token_file", ""));
            String curseToken = tokenExtractor.getCurseForgeToken();
            if(curseToken == null || curseToken.trim().isEmpty()){
                curseTokenField.setText("Error: CurseForge token missing!");
                curseTokenField.setForeground(Color.RED);
            } else {
                curseTokenField.setText(curseToken);
            }
        } catch(Exception ex) {
            curseTokenField.setText("Error: Token file not found or token missing in file!");
            curseTokenField.setForeground(Color.RED);
        }
        panel.add(createRow("CurseForge Token:", curseTokenField));

        return panel;
    }

    private JPanel createModrinthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        modrinthProjectIdField = new JTextField(config.getOrDefault("modrinth_project_id_or_slug", "").toString());
        modrinthFeaturedCheck = new JCheckBox();
        modrinthFeaturedCheck.setSelected(Boolean.parseBoolean(config.getOrDefault("modrinth_featured", false).toString()));

        panel.add(createRow("Project ID/Slug:", modrinthProjectIdField));
        panel.add(createRow("Featured:", modrinthFeaturedCheck));

        // Add row for Modrinth token.
        modrinthTokenField = new JTextField();
        modrinthTokenField.setEditable(false);
        try {
            TokenExtractor tokenExtractor = new TokenExtractor(ModManager.config.getOrDefault("token_file", ""));
            String modrinthToken = tokenExtractor.getModrinthToken();
            if(modrinthToken == null || modrinthToken.trim().isEmpty()){
                modrinthTokenField.setText("Error: Modrinth token missing!");
                modrinthTokenField.setForeground(Color.RED);
            } else {
                modrinthTokenField.setText(modrinthToken);
            }
        } catch(Exception ex) {
            modrinthTokenField.setText("Error: Token file not found or token missing in file!");
            modrinthTokenField.setForeground(Color.RED);
        }
        panel.add(createRow("Modrinth Token:", modrinthTokenField));

        return panel;
    }

    private JPanel createRow(String labelText, JComponent inputComponent) {
        JPanel row = new JPanel(new BorderLayout(5, 5));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25));
        row.add(label, BorderLayout.WEST);
        inputComponent.setPreferredSize(new Dimension(400, 25));
        row.add(inputComponent, BorderLayout.CENTER);
        return row;
    }

    private JPanel createTextAreaRow(String labelText, JScrollPane scrollPane) {
        JPanel row = new JPanel(new BorderLayout(5, 5));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25));
        row.add(label, BorderLayout.WEST);
        row.add(scrollPane, BorderLayout.CENTER);
        return row;
    }

    private void loadGameVersions() {
        refreshVersionsButton.setEnabled(false);
        gameVersionsModel.clear();
        saveButton.setEnabled(false);  // disable Save while loading

        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                TokenExtractor tokenExtractor = new TokenExtractor(ModManager.config.getOrDefault("token_file", ""));
                String token = Objects.requireNonNullElse(tokenExtractor.getCurseForgeToken(), "");
                String endpoint = config.getOrDefault("endpoint", "minecraft").toString();
                CurseForge cf = new CurseForge(endpoint, token);
                List<CurseGameVersion> versions = cf.getGameVersions();
                return versions.stream()
                        .map(v -> v.name)
                        .distinct()
                        .collect(Collectors.toList());
            }
            @Override
            protected void done() {
                try {
                    List<String> versionNames = get();
                    gameVersionsLoaded = true;
                    String selectedVersionsConfig = config.getOrDefault("game_versions", "").toString().trim();
                    String[] selectedVersions = selectedVersionsConfig.isEmpty() ? new String[0] : selectedVersionsConfig.split("\\s*,\\s*");
                    for (String name : versionNames) {
                        CheckListItem item = new CheckListItem(name);
                        String processed = processVersionString(name);
                        for (String sel : selectedVersions) {
                            if (sel.equalsIgnoreCase(processed)) {
                                item.setSelected(true);
                                break;
                            }
                        }
                        gameVersionsModel.addElement(item);
                    }
                } catch (Exception ex) {
                    gameVersionsLoaded = false;
                    JOptionPane.showMessageDialog(ConfigView.this,
                            "Failed to load game versions: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                refreshVersionsButton.setEnabled(true);
                saveButton.setEnabled(true);  // re-enable Save after load attempt
            }
        };
        worker.execute();
    }

    private String processVersionString(String version) {
        Pattern pattern = Pattern.compile("^(\\d+\\.\\d+)(\\.\\d+)?$");
        Matcher matcher = pattern.matcher(version);
        if(matcher.matches()) {
            String major = matcher.group(1);
            return version + ":Minecraft " + major;
        }
        return version;
    }

    private void saveConfig() {
        try {
            config.setValue("mod_jar_name", modJarNameField.getText());
            config.setValue("sources_jar_name", sourcesJarNameField.getText());
            config.setValue("rename_jar_to", renameJarToField.getText());
            config.setValue("dev_jar_prefix", devJarPrefixField.getText());
            config.setValue("build_classes_dir", buildClassesDirField.getText());
            config.setValue("src_dir", srcDirField.getText());
            config.setValue("build_jar_dir", buildJarDirField.getText());
            config.setValue("output_dir", outputDirField.getText());
            config.setValue("display_name", curseDisplayNameField.getText());
            config.setValue("upload_from_folder", uploadFromFolderField.getText());
            config.setValue("mod_relations", modRelationsField.getText());

            // Only update game_versions if the load succeeded.
            if (gameVersionsLoaded) {
                StringBuilder versionsValue = new StringBuilder();
                for (int i = 0; i < gameVersionsModel.getSize(); i++) {
                    CheckListItem item = gameVersionsModel.getElementAt(i);
                    if(item.isSelected()) {
                        String processed = processVersionString(item.getLabel());
                        if(versionsValue.length() > 0) {
                            versionsValue.append(",");
                        }
                        versionsValue.append(processed);
                    }
                }
                config.setValue("game_versions", versionsValue.toString());
            }
            config.setValue("release_type", (String) releaseTypeCombo.getSelectedItem());
            config.setValue("changelog_type", (String) changelogTypeCombo.getSelectedItem());
            config.setValue("changelog", changelogTextArea.getText().replace("\n", "%n%"));
            config.setValue("dev_file_description", devFileDescriptionField.getText());

            config.setValue("endpoint", curseEndpointField.getText());
            try {
                long projId = Long.parseLong(curseProjectIdField.getText());
                config.setValue("project_id", projId);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid CurseForge Project ID", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            config.setValue("modrinth_project_id_or_slug", modrinthProjectIdField.getText());
            config.setValue("modrinth_featured", modrinthFeaturedCheck.isSelected());

            config.syncConfig();

            // Show a different popup message if game versions were not updated
            if (gameVersionsLoaded) {
                JOptionPane.showMessageDialog(this, "Configuration saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Configuration saved successfully, but game versions were not updated because the game versions list failed to load.", "Partial Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save configuration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class CheckListItem {
        private String label;
        private boolean isSelected;

        public CheckListItem(String label) {
            this.label = label;
            this.isSelected = false;
        }
        public String getLabel() {
            return label;
        }
        public boolean isSelected() {
            return isSelected;
        }
        public void setSelected(boolean selected) {
            isSelected = selected;
        }
        @Override
        public String toString() {
            return label;
        }
    }

    private static class CheckListRenderer extends JCheckBox implements ListCellRenderer<CheckListItem> {
        @Override
        public Component getListCellRendererComponent(JList<? extends CheckListItem> list, CheckListItem value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setEnabled(list.isEnabled());
            setSelected(value.isSelected());
            setFont(list.getFont());
            setText(value.getLabel());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }
}
