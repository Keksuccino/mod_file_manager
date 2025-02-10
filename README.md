# Minecraft Mod Upload Tool

Welcome to the repository for the tool that prepares and uploads my Minecraft mods. Originally a personal experiment, the code remains a bit messy and hardcoded in parts, but feel free to explore, learn from, or even contribute to it.

This tool isn’t just a simple uploader. It provides a complete workflow for:
- **Preparing mod files** (renaming build JARs and copying them to an output folder)  
- **Uploading mods** to both CurseForge and Modrinth  
- **Managing configurations** and tokens through a built-in Swing-based GUI  
- **Handling mod metadata** such as game versions, release types, changelogs, and dependency relations

<br>
<img width="600" alt="Screenshot_1" src="https://github.com/user-attachments/assets/4d642cb2-3e7e-4e2e-b1ad-0ee8be364fd8" />

## Important Information

- **Multi-Loader Setup:**  
  The current implementation requires a multi-loader environment. A `gradle.properties` file must be placed in the parent directory containing a `mod_version` variable. This property is read by the tool to determine the mod version, which can either be a plain version string (e.g., `1.0.0`) or a path to a text file that holds the version.

- **Configuration Details:**  
  All settings are stored in a local `config.txt` file. The tool uses a custom configuration system that supports multiple data types (strings, booleans, integers, etc.). You can adjust parameters like jar names, directories, display names, game versions, and more using the provided GUI (or by editing the file directly).

- **Token File Format:**  
  The location of the token file isn’t hardcoded, but you need to manually set it in the config file. The file should contain tokens on separate lines in the following format (replace `<token>` with your actual API token):
  ```
  curseforge:<token>
  modrinth:<token>
  ```

- **Prepare Mod Files Button:**  
  Clicking the **Prepare Mod Files** button will automatically rename your build JARs and place them in the default `jar_output` folder. The renaming template is configurable and may use placeholders (e.g., `%version%`, `%mc_version_range%`, `%loaders%`) that the tool replaces based on your configuration and detected game version ranges.

- **Bug Notice:**  
  As this tool is still experimental, some bugs may occur. It is highly recommended to test its functionality on a test project for both Modrinth and CurseForge before using it in a production environment.

## Configuration & Usage

1. **Setup:**
   - The tool is made for multi-loader projects, so it is recommended to only use it in the sub projects (`fabric`, `forge`, `neoforge`) of multi-loader projects.
   - Create a `gradle.properties` file in the parent directory with at least the `mod_version` variable. The tool needs that file to get the mod version.
   - Prepare a token file (the location is set in `config.txt`) with your CurseForge and Modrinth tokens using the required format.

2. **Configuring the Tool:**
   - Run the tool’s GUI (the main window appears via `MainView`).
   - Use the **Open Config** button to adjust values such as jar names, directories, display names, game versions, endpoint URLs, project IDs, and changelog text.
   - Many placeholders (e.g., `%version%`, `%mc_version_range%`, `%loaders%`) are available in file renaming and display templates.

3. **Preparing Mod Files:**
   - Make sure the output folder is **EMPTY** before preparing mod files, since overriding JAR files via the tool caused some problems in the past.
   - Click **Prepare Mod Files**. The tool will rename and copy your build jars from the specified build directory to the output directory.
   - After preparing the files, it is recommended to check the output folder to make sure everything worked correctly.

4. **Uploading:**
   - You can choose to upload only to CurseForge, only to Modrinth, or both.  
   - The tool validates that tokens are present and that the project IDs (or slug for Modrinth) are correctly configured.
   - Upload progress is shown via a progress bar, and detailed log messages are printed to the console.

5. **Error Handling:**
   - The tool checks for missing tokens, invalid file paths, or configuration issues and will display error messages accordingly.
   - Always check the console output for informational “INFO” and error “ERROR” messages during preparation and upload processes.

## License

This project is licensed under the MIT License.

```
MIT License

Copyright (c) 2025 Keksuccino

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

Feel free to fork, modify, or provide feedback. Happy modding!

