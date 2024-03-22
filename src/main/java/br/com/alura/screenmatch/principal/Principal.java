package br.com.alura.screenmatch.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

public class Principal {
    
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERCO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=2899ecac";

    public void exibeMenu(){
        System.out.println("Digite o nome da série para busca:");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERCO + nomeSerie.replace(" ", "+") + API_KEY);
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

		for(int i = 1; i <= dados.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERCO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			System.out.println(dadosTemporada);
			temporadas.add(dadosTemporada);
		}

		temporadas.forEach(System.out::println);

        for(int i = 0; i < dados.totalTemporadas(); i++) {
            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodio();
            for(int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }

        temporadas.forEach(t -> t.episodio().forEach(e -> System.out.println(e.titulo())));

        // List<String> nomes = Arrays.asList("Jacque", "Iasmin", "Paulo", "Rodrigo", "Nico");
        
        // nomes.stream()
        //     .sorted()
        //     .limit(3)
        //     .filter(n -> n.startsWith("N"))
        //     .map(n -> n.toUpperCase())
        //     .forEach(System.out::println);
    
        List <DadosEpisodio> dadosEpisodios = temporadas.stream()
            .flatMap(t -> t.episodio().stream())
            .collect(Collectors.toList());
        
        System.out.println("\nTop 5 episodios");
        dadosEpisodios.stream()
            .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
            .sorted(Comparator.comparing(DadosEpisodio::avaliacao)
            .reversed()).limit(5)
            .forEach(System.out::println);
        
        List<Episodio> episodios = temporadas.stream()
        .flatMap(t -> t.episodio().stream()
                .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());
        
        episodios.forEach(System.out::println);

        System.out.println("A partir de que ano você deseja ver os episodios? ");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                    "Temporada: " + e.getTemporada() +
                    " Episodio: " + e.getTitulo() +
                    " Data Lançamento " + e.getDataLancamento().format(formatador)
                ));
    }
    
}
