package org.example;

import java.io.*;
import java.util.*;

public class CommandLineInterpreter {
    private File currentDirectory;
    private File previousDirectory;
    public CommandLineInterpreter() {
        this.currentDirectory = new File(System.getProperty("user.home"));
    }

    public String pwd() {
        return currentDirectory.getAbsolutePath();
    }

    public void cd(String path) {
        previousDirectory = currentDirectory; // Store the current directory

        File newDir = new File(path);
        if (newDir.isAbsolute()) {
            if (newDir.exists() && newDir.isDirectory()) {
                currentDirectory = newDir;
            } else {
                System.out.println("Error: Not a directory");
            }
        } else {
            File relativeDir = new File(currentDirectory, path);
            if (relativeDir.exists() && relativeDir.isDirectory()) {
                currentDirectory = relativeDir;
            } else {
                System.out.println("Error: Not a directory");
            }
        }
    }


    // cd - command to go back to the previous directory
    public void cdPrevious() {
        if (previousDirectory != null) {
            currentDirectory = previousDirectory;
            previousDirectory = null; // Reset previousDirectory after using it
        } else {
            System.out.println("Error: No previous directory.");
        }
    }

    public String[] ls(boolean showHidden, boolean reverseOrder) {
        File[] files = currentDirectory.listFiles();
        if (files == null) return new String[0];

        List<String> fileNames = new ArrayList<>();
        for (File file : files) {
            if (showHidden || !file.isHidden()) {
                fileNames.add(file.getName());
            }
        }

        if (reverseOrder) {
            fileNames.sort(Collections.reverseOrder());
        } else {
            Collections.sort(fileNames);
        }
        return fileNames.toArray(new String[0]);
    }

    public void mkdir(String dirName) {
        File newDir = new File(currentDirectory, dirName);
        if (!newDir.exists()) {
            newDir.mkdir();
        } else {
            System.out.println("Error: Directory already exists");
        }
    }

    public void rmdir(String dirName) {
        File dir = new File(currentDirectory, dirName);
        if (dir.exists() && dir.isDirectory()) {
            if (dir.list().length == 0) {
                dir.delete();
            } else {
                System.out.println("Error: Directory is not empty");
            }
        } else {
            System.out.println("Error: Directory does not exist");
        }
    }

    public void touch(String fileName) {
        File file = new File(currentDirectory, fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                System.out.println("Error: File already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mv(String source, String destination) {
        File srcFile = new File(currentDirectory, source);
        File destFile = new File(currentDirectory, destination);
        if (srcFile.exists()) {
            srcFile.renameTo(destFile);
        } else {
            System.out.println("Error: Source file does not exist");
        }
    }

    public void rm(String fileName) {
        File file = new File(currentDirectory, fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        } else {
            System.out.println("Error: File does not exist");
        }
    }

    public void cat(String fileName) {
        File file = new File(currentDirectory, fileName);
        if (file.exists() && file.isFile()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error: File does not exist");
        }
    }



    public void help() {
        System.out.println("Available commands:");
        System.out.println("pwd - Print current directory");
        System.out.println("cd <path> - Change directory");
        System.out.println("ls [-a] [-r] - List files");
        System.out.println("mkdir <dir> - Make a new directory");
        System.out.println("rmdir <dir> - Remove a directory");
        System.out.println("touch <file> - Create a new file");
        System.out.println("mv <source> <destination> - Move/rename a file");
        System.out.println("rm <file> - Remove a file");
        System.out.println("cat <file> - Print the content of a file");
        System.out.println("help - Display this help message");
        System.out.println("exit - Exit the CLI");
    }

    public static void main(String[] args) {
        CommandLineInterpreter cli = new CommandLineInterpreter();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the CLI! Type 'help' for a list of commands.");

        while (true) {
            System.out.print(cli.pwd() + " $ ");
            String input = scanner.nextLine().trim();

            // Handle normal commands
            String[] commandParts = input.split(" ");
            String command = commandParts[0];

            // Check for redirection operators
            String operator = ""; // Declare operator here
            String fileName = ""; // Declare fileName here
            if (input.contains(">") || input.contains(">>")) {
                String[] parts = input.split(">");
                command = parts[0].trim();
                operator = ">";
                if (parts.length > 1) {
                    fileName = parts[1].trim();
                    if (fileName.startsWith(">")) { // Handle double operator
                        operator = ">>";
                        fileName = fileName.substring(1).trim();
                    }
                }
            }

            // Execute commands
            switch (command) {
                case "pwd":
                    System.out.println(cli.pwd());
                    break;
                case "cd":
                    if (commandParts.length > 1) { // Change this from params to commandParts
                        if ("-".equals(commandParts[1])) { // Check for cd -
                            cli.cdPrevious();
                        } else {
                            cli.cd(commandParts[1]);
                        }
                    } else {
                        System.out.println("Error: Path not specified.");
                    }
                    break;
                case "ls":
                    boolean showHidden = Arrays.asList(commandParts).contains("-a");
                    boolean reverseOrder = Arrays.asList(commandParts).contains("-r");
                    String[] files = cli.ls(showHidden, reverseOrder);
                    System.out.println(String.join("\n", files));
                    break;
                case "mkdir":
                    if (commandParts.length > 1) {
                        cli.mkdir(commandParts[1]);
                    } else {
                        System.out.println("Error: Directory name not specified.");
                    }
                    break;
                case "rmdir":
                    if (commandParts.length > 1) {
                        cli.rmdir(commandParts[1]);
                    } else {
                        System.out.println("Error: Directory name not specified.");
                    }
                    break;
                case "touch":
                    if (commandParts.length > 1) {
                        cli.touch(commandParts[1]);
                    } else {
                        System.out.println("Error: File name not specified.");
                    }
                    break;
                case "mv":
                    if (commandParts.length == 3) {
                        cli.mv(commandParts[1], commandParts[2]);
                    } else {
                        System.out.println("Error: Source and destination not specified.");
                    }
                    break;
                case "rm":
                    if (commandParts.length > 1) {
                        cli.rm(commandParts[1]);
                    } else {
                        System.out.println("Error: File name not specified.");
                    }
                    break;
                case "cat":
                    if (commandParts.length > 1) {
                        cli.cat(commandParts[1]);
                    } else {
                        System.out.println("Error: File name not specified.");
                    }
                    break;
                case "help":
                    cli.help();
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Error: Unknown command.");
            }
        }
    }
}
