import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.util.Scanner;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by rhmclaessens on 15-05-2014.
 */
public class Labeler {
    private String[] tagsToLabel = new String[]{"chapter", "section", "subsection", "subsubsection"};
    private String[] labels = new String[]{"chap", "sec", "subsec", "subsubsec"};
    private String[] escapeLabels = new String[]{"ac", "acl", "acp", "aclp", "acf"};
    private String[][] replaceInLabels = new String[][]{{"\\\\&", "and"}, {"\\$", ""}, {"\\\\", ""}};
    private int numberOfLabels = 0;
    private boolean appendix = false;
    private String texRootLabel = "% !TEX root = ";
    private String texProgramLabel = "%!TEX program";
    private int numberOfFilesUpdated = 0;
    private Ansi.Color messageColor = DEFAULT;
    private Ansi.Color modifyColor = GREEN;
    private Ansi.Color folderColor = CYAN;
    private Ansi.Color fileColor = GREEN;
    private Ansi.Color chapterColor = BLUE;

    public static void main(String[] args) {
        System.out.println("\n\n==========================================================================================");
        if (args.length < 2) {
            System.err.println("Provide the path to the folder you wish to label and the root tex file of the document");
            System.exit(0);
        }
        new Labeler().label(args[0], args[1]);
        System.out.println("==========================================================================================\n\n");
    }

    public void initJansi() {
        AnsiConsole.systemInstall();
    }

    public String printAnsi(String s, Ansi.Color color) {
        return ansi().fg(color).a(s).reset().toString();
    }

    public void label(String path, String root) {
        initJansi();
        System.out.println(printAnsi("Labeling: ", messageColor) + printAnsi(path, modifyColor));
        System.out.println(printAnsi("TEX root: ", messageColor) + printAnsi(root, modifyColor));
        File folder = new File(path);
        if (!folder.exists()) {
            System.err.println("Folder " + folder.getPath() + " doesn't exist");
            System.exit(0);
        }
        labelFolder(folder, 0, root, folder.getName());
        labelFolder(folder, 0, root, folder.getName());
        System.out.println(printAnsi("\nAdded ", messageColor) + printAnsi(Integer.toString(numberOfLabels), modifyColor) + printAnsi(" label" + ((numberOfLabels > 1 || numberOfLabels == 0) ? "s" : "") + ".", messageColor));
        System.out.println(printAnsi("Updated ", messageColor) + printAnsi(Integer.toString(numberOfFilesUpdated), modifyColor) + printAnsi(" file" + ((numberOfFilesUpdated > 1 || numberOfFilesUpdated == 0) ? "s" : "") + ".", messageColor));
    }

    public void labelFolder(File folder, int depth, String root, String path) {
        String chapterTitle = lookForChapterTitleInFolder(folder);
        if (!chapterTitle.equalsIgnoreCase("")) {
            System.out.println(printAnsi(getTabs(depth) + "#  " + chapterTitle, chapterColor));
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                System.out.println(printAnsi(getTabs(depth + 1) + "> " + file.getName(), folderColor));
                labelFolder(file, depth + 1, root, path + "/" + file.getName());
            } else if (getExtension(file).equalsIgnoreCase(".tex")){
                labelFile(file, depth, root, path, chapterTitle);
            }
        }
    }

    public String lookForChapterTitleInFolder(File folder) {
        String chapterTitle = "";
        File chapterFile = new File(folder.getPath() + "/" + folder.getName().toLowerCase().replaceAll(" ", "") + ".tex");
        if (chapterFile.exists()) {
            chapterTitle = lookForChapterTitleInFile(chapterFile);
        }
        return chapterTitle;
    }

    public String lookForChapterTitleInFile(File file) {
        String chapterTitle = "";
        try {
            Scanner scanner =  new Scanner(file);
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                for (int i = 0; i < tagsToLabel.length; i++) {
                    if (nextLine.contains("\\" + tagsToLabel[i] + "{")) {
                        String title = nextLine.substring(nextLine.indexOf('{') + 1, nextLine.length() - 1);
                        if (nextLine.contains("chapter")) {
                            chapterTitle = (appendix ? "app-" : "") + title;
                            return chapterTitle;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return chapterTitle;
    }

    public void labelFile(File file, int depth, String root, String path, String chapterTitle) {
//        System.out.println("Labeling " + file.getPath());
        String s = "";
        boolean print = true;
        boolean firstLine = true;
        StringBuilder texRootBuilder = new StringBuilder();
        texRootBuilder.append(texRootLabel);
        for (int i = 0; i < depth; i++) {
            texRootBuilder.append("../");
        }
        texRootBuilder.append(root);
        String texRoot = texRootBuilder.toString();
        try {
            Scanner scanner =  new Scanner(file);
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                if (firstLine) {
                    if (nextLine.contains(texRootLabel)){
                        if (!nextLine.equalsIgnoreCase(texRoot)) {
                            nextLine = texRoot;
                        }
                    } else {
                        nextLine = texRootBuilder.toString() + "\n" + nextLine;
                    }
                    firstLine = false;
                }
                for (int i = 0; i < tagsToLabel.length; i++) {
                    if (nextLine.contains("\\begin{appendices}")) {
                        appendix = true;
                    }
                    if (nextLine.contains("\\end{appendices}")) {
                        appendix = false;
                    }
                    if (nextLine.contains("\\" + tagsToLabel[i] + "{")) {
                        String title = nextLine.substring(nextLine.indexOf('{') + 1, nextLine.length() - 1);
                        if (nextLine.contains("chapter")) {
                            chapterTitle = (appendix ? "app-" : "") + title;
                        }
//                        System.out.println("\tFound a " + tagsToLabel[i] + " tag with title " + title + " in " + nextLine);
                        s += nextLine + "\n";
                        if (scanner.hasNextLine()) {
                            nextLine = scanner.nextLine();
                            if (nextLine.contains("\\label")) {
                                nextLine = "";
                            }
                        }
                        String prefix = "";
                        if (!tagsToLabel[i].equalsIgnoreCase("chapter")) {
                            prefix = chapterTitle.replaceAll(" ", "").toLowerCase() + "-" + labels[i] + ":";
                        }
                        String label = prefix + title.replaceAll(" ", "").toLowerCase();
//                        System.out.println("\t\tGoing to add label in file " + file.getPath() + " : " + label.replaceAll("\n", ""));
                        for (String escapeLabel : escapeLabels) {
                            label = label.replaceAll("\\\\" + escapeLabel + "\\{", "");
                        }
                        for (String[] replaceInLabel : replaceInLabels) {
                            label = label.replaceAll(replaceInLabel[0], replaceInLabel[1]);
                        }
                        label = label.replaceAll("}", "");
                        label = "\\label{" + label + "}\n";
                        s += label + (!nextLine.equalsIgnoreCase("") ? nextLine : "");
                        nextLine = "";
                        print = false;
                        numberOfLabels++;
                    }
                }
                if (print)
                    s += nextLine + (scanner.hasNextLine() ? "\n" : "");
                print = true;
            }
            String original = "";
            try {
                original = readFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!s.equalsIgnoreCase(original) && !s.equalsIgnoreCase(original + "\n")) {
                System.out.println(printAnsi(getTabs(depth) + "|  " + file.getName() + " updated", modifyColor));
                numberOfFilesUpdated++;
                Writer writer = null;

                try {
                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(file.getPath()), "utf-8"));
                    writer.write(s);
                } catch (IOException ex) {
                    // report
                } finally {
                    try {writer.close();} catch (Exception ex) {}
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getExtension(String path) {
        if (path.lastIndexOf('.') == -1) return "";
        return path.substring(path.lastIndexOf('.'), path.length());
    }

    public String getExtension(File file) {
        return getExtension(file.getPath());
    }

    public String removeExtension(String path) {
        if (path.lastIndexOf('.') == -1) return "";
        return path.substring(0, path.lastIndexOf('.'));
    }

    public static String readFile(File file) throws IOException {
        return readFile(file.getPath());
    }

    public static String readFile(String pathname) throws IOException {

        File file = new File(pathname);
//        System.out.println("Expecting settings file at " + file.getPath());
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");
        boolean firstLine = true;

        try {
            while(scanner.hasNextLine()) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    fileContents.append(lineSeparator);
                }
                fileContents.append(scanner.nextLine());
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

    public String getTabs(int tabs) {
        StringBuilder tabBuilder = new StringBuilder();
        for (int i = 0; i < tabs; i++) {
            tabBuilder.append("  ");
        }
        return tabBuilder.toString();
    }
}
