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
        previousDirectory = currentDirectory;
        File newDir = new File(path);
        if (newDir.isAbsolute() ? newDir.isDirectory() : new File(currentDirectory, path).isDirectory()) {
            currentDirectory = newDir.isAbsolute() ? newDir : new File(currentDirectory, path);
        } else {
            System.out.println("Error: Not a directory");
        }
    }

    public String[] ls(boolean showHidden, boolean reverseOrder) {
        File[] files = currentDirectory.listFiles();
        if (files == null) return new String[0];
        List<String> fileNames = new ArrayList<>();
        for (File file : files) {
            if (showHidden || !file.isHidden()) fileNames.add(file.getName());
        }
        fileNames.sort(reverseOrder ? Collections.reverseOrder() : String::compareTo);
        return fileNames.toArray(new String[0]);
    }



    public void mkdir(String... dirNames) {
        for (String dirName : dirNames) {
            File newDir = new File(currentDirectory, dirName);
            if (!newDir.exists()) newDir.mkdir();
            else System.out.println("Error: Directory " + dirName + " already exists");
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

    public void touch(String... fileNames) {
        for (String fileName : fileNames) {
            File file = new File(currentDirectory, fileName);
            try {
                if (!file.exists()) file.createNewFile();
                else System.out.println("Error: File " + fileName + " already exists");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void mv(String source, String destination) {
        File srcFile = new File(currentDirectory, source);
        File destFile = new File(currentDirectory, destination);

        if (srcFile.exists()) {
            if (destFile.isDirectory()) {
                destFile = new File(destFile, srcFile.getName());
            }
            boolean success = srcFile.renameTo(destFile);
            if (!success) {
                System.out.println("Error: Could not move file.");
            }
        } else {
            System.out.println("Error: Source file does not exist");
        }
    }


    public void rm(String fileName) {
        File file = new File(currentDirectory, fileName);
        if (file.exists() && file.isFile()) file.delete();
        else System.out.println("Error: File does not exist");
    }

    public void cat(String fileName) {
        File file = new File(currentDirectory, fileName);
        if (file.exists() && file.isFile()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else System.out.println("Error: File does not exist");
    }

    public void writeToFile(String fileName, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(currentDirectory, fileName), append))) {
            System.out.println("Enter the content you want to write :");
            Scanner scanner = new Scanner(System.in);
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.equals("exit")) break;
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void help() {
        System.out.println("Available commands:");
        System.out.println("pwd - Print current directory");
        System.out.println("cd <path> - Change directory");
        System.out.println("ls [-a] [-r] - List files");
        System.out.println("mkdir <dir> - Make directories");
        System.out.println("rmdir <dir> - Remove a directory");
        System.out.println("touch <file> - Create files");
        System.out.println("mv <source> <destination> - Move/rename files");
        System.out.println("rm <file> - Remove a file");
        System.out.println("cat <file> - Print file contents");
        System.out.println("help - Display help message");
        System.out.println("exit - Exit the CLI");
    }

    public static void main(String[] args) {
        CommandLineInterpreter cli = new CommandLineInterpreter();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the CLI! Type 'help' for a list of commands.");

        while (true) {
            System.out.print(cli.pwd() + " $ ");
            String input = scanner.nextLine().trim();

            String[] commandParts = input.split("\\s+");
            String command = commandParts[0];

            boolean append = input.contains(">>");
            boolean redirect = input.contains(">");
            boolean pipe = input.contains("|");

            try {
                switch (command) {
                    case "pwd":
                        System.out.println(cli.pwd());
                        break;
                    case "cd":
                        cli.cd(commandParts.length > 1 ? commandParts[1] : ".");
                        break;
                    case "ls":
                        boolean showHidden = input.contains("-a");
                        boolean reverseOrder = input.contains("-r");
                        String[] files = cli.ls(showHidden, reverseOrder);
                        System.out.println(String.join("\n", files));
                        break;
                    case "mkdir":
                        cli.mkdir(Arrays.copyOfRange(commandParts, 1, commandParts.length));
                        break;
                    case "rmdir":
                        if (commandParts.length > 1) {
                            cli.rmdir(commandParts[1]);
                        } else {
                            System.out.println("Error: Directory name not specified.");
                        }
                        break;
                    case "touch":
                        cli.touch(Arrays.copyOfRange(commandParts, 1, commandParts.length));
                        break;
                    case "mv":
                        if (commandParts.length == 3) cli.mv(commandParts[1], commandParts[2]);
                        else System.out.println("Error: Source and destination not specified.");
                        break;
                    case "rm":
                        cli.rm(commandParts[1]);
                        break;
                    case "cat":
                        if (redirect || append) {
                            String fileName = input.split(">(>?)\\s*")[1].trim();
                            cli.writeToFile(fileName, append);
                        } else if (pipe) {
                        } else if (commandParts.length > 1) {
                            cli.cat(commandParts[1]);
                        } else {
                            System.out.println("Error: File name not specified for cat command.");
                        }
                        break;
                    case "help":
                        cli.help();
                        break;
                    case "exit":
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Error: Unknown command.");
                }
            } catch (Exception e) {
                System.out.println("Error: Invalid input");
            }
        }
    }
}

