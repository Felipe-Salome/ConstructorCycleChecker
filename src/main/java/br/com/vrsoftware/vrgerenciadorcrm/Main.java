package br.com.vrsoftware.vrgerenciadorcrm;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Main {
    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+(\\w+)");
    private static final Pattern CONSTRUCTOR_PATTERN = Pattern.compile("(\\w+)\\s*\\(");
    private static final Pattern CALL_PATTERN = Pattern.compile("(new\\s+(\\w+)\\(|VRInstance\\.criar\\((\\w+)\\.class\\))");

    public static void verificarCiclosConstrutores(Path diretorio) throws IOException {
        Map<String, Set<String>> grafoConstrutores = new HashMap<>();

        Files.walk(diretorio)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> processarArquivo(path, grafoConstrutores));

        for (String classe : grafoConstrutores.keySet()) {
            if (temCicloConstrutor(classe, grafoConstrutores, new HashSet<>())) {
                System.out.println("Ciclo de construtor detectado na classe: " + classe);
            }
        }
    }

    private static void processarArquivo(Path arquivo, Map<String, Set<String>> grafoConstrutores) {
        try {
            List<String> linhas = Files.readAllLines(arquivo);
            String classeAtual = null;
            Set<String> dependencias = new HashSet<>();

            for (String linha : linhas) {
                Matcher classeMatcher = CLASS_PATTERN.matcher(linha);
                if (classeMatcher.find()) {
                    classeAtual = classeMatcher.group(1);
                }

                if (classeAtual != null) {
                    Matcher construtorMatcher = CONSTRUCTOR_PATTERN.matcher(linha);
                    if (construtorMatcher.find() && construtorMatcher.group(1).equals(classeAtual)) {
                        Matcher callMatcher = CALL_PATTERN.matcher(linha);
                        while (callMatcher.find()) {
                            String chamada = callMatcher.group(2) != null ? callMatcher.group(2) : callMatcher.group(3);
                            dependencias.add(chamada);
                        }
                    }
                }
            }

            if (classeAtual != null && !dependencias.isEmpty()) {
                grafoConstrutores.put(classeAtual, dependencias);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean temCicloConstrutor(String classe, Map<String, Set<String>> grafo, Set<String> visitados) {
        if (visitados.contains(classe)) {
            return true;
        }

        if (!grafo.containsKey(classe)) {
            return false;
        }

        visitados.add(classe);
        for (String dependencia : grafo.get(classe)) {
            if (temCicloConstrutor(dependencia, grafo, new HashSet<>(visitados))) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        Path diretorio = Paths.get("C:\\git\\main\\VRPdv\\src\\main\\java\\vrpdv");
        verificarCiclosConstrutores(diretorio);
    }
}