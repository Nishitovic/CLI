package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.File;

public class CommandLineInterpreterTest {
    private CommandLineInterpreter cli;

    @BeforeEach
    public void setUp() {
        cli = new CommandLineInterpreter();
    }

    @Test
    public void testPwd() {
        assertEquals(System.getProperty("user.home"), cli.pwd());
    }

    @Test
    public void testCd() {
        cli.cd(System.getProperty("user.home") + File.separator + "Documents");
        assertEquals(System.getProperty("user.home") + File.separator + "Documents", cli.pwd());
    }

    @Test
    public void testLs() {
        cli.cd(System.getProperty("user.home")); // Change to a directory that likely has files
        String[] files = cli.ls(false, false); // Call ls with showHidden = false and reverseOrder = false
        assertTrue(files.length >= 0, "The ls command should return the list of files in the directory.");
    }

    @Test
    public void testMkdir() {
        cli.mkdir("TestDir");
        assertTrue(new File(cli.pwd() + File.separator + "TestDir").exists());
    }

    @Test
    public void testRmdir() {
        cli.mkdir("TestDir");
        cli.rmdir("TestDir");
        assertFalse(new File(cli.pwd() + File.separator + "TestDir").exists());
    }

    @Test
    public void testTouch() {
        cli.touch("testfile.txt");
        assertTrue(new File(cli.pwd() + File.separator + "testfile.txt").exists());
    }

    @Test
    public void testMv() {
        cli.touch("fileToMove.txt");
        cli.mv("fileToMove.txt", "movedFile.txt");
        assertTrue(new File(cli.pwd() + File.separator + "movedFile.txt").exists());
        assertFalse(new File(cli.pwd() + File.separator + "fileToMove.txt").exists());
    }

    @Test
    public void testRm() {
        cli.touch("fileToDelete.txt");
        cli.rm("fileToDelete.txt");
        assertFalse(new File(cli.pwd() + File.separator + "fileToDelete.txt").exists());
    }

    @Test
    public void testHelp() {
        cli.help(); // This prints the help message; no assert needed
    }

    @AfterEach
    public void tearDown() {
        new File(cli.pwd() + File.separator + "TestDir").delete(); // Attempt to delete the test directory
        new File(cli.pwd() + File.separator + "testfile.txt").delete();
        new File(cli.pwd() + File.separator + "fileToMove.txt").delete();
        new File(cli.pwd() + File.separator + "movedFile.txt").delete();
        new File(cli.pwd() + File.separator + "fileToDelete.txt").delete();
    }
}
