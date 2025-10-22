import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.nio.file.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.List;
/**
 Universidad Diego Portales - EDA 2025
 LABORATORIO 3: Algoritmos de Ordenamiento y Búsqueda
 UNIVERSIDAD DIEGO PORTALES
 PROFESOR: Cristian Llull
 INTEGRANTES:
 -Cinthya Fuentealba Bravo
 -Ignacia Reyes Ojeda
 */


//========================================================================================================================================================================================================
class CsvIMDBLoader {

    // Divide por comas literales
    private static final Pattern SPLIT = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    private static String clean(String celda) {
        //Si el valor es nulo, no hay nada para limpiar
        if (celda == null) {return null;}

        //Se quitan los espacios del contenido
        celda = celda.trim();

        //Si se comienza o termina con " " y tiene al menos largo mayor a 2 caracteres
        if (celda.startsWith("\"") && celda.endsWith("\"") && celda.length() >= 2) {
            //Se sustraen las comillas externas
            celda = celda.substring(1, celda.length() - 1);
        }
        //Nuevamente quitamos espacios por si habian dentro de las comillas externas
        return celda.trim();
    }

    //Convierte una cadena de año a un int, sino pudiese retorna 0
    private static int cadenaaYear(String cYear) {
        //Si esta nulo se retorna 0
        if ( cYear == null) {return 0;}

        // Cuando el texto viene como por ejemplo "1994" o "1994(I)" , se dejan solo los digitos
        String numeroYear = cYear.replaceAll("[^0-9-]", "");

        //Si se limpio y no quedo nada, se retorna 0
        if (numeroYear.isEmpty()) {return 0;}

        //try-catch: Atrapa errores. El try inta ejecutar el boque..... . Si pasa el try ocurre el catch y maneja el error
        try { return Integer.parseInt(numeroYear); }
        catch (Exception e) { return 0; }
    }
    //Convierte un texto a un double, sino se puede se retorna 0
    private static double textoaDouble(String palabra) {
        //El texto es nulo, retorna 0
        if (palabra == null) {return 0.0;}

        //Quita los espacios y semplaza de coma a punto
        try { return Double.parseDouble(palabra.trim().replace(",", ".")); }
        //No se puede, retorna 0
        catch (Exception e) { return 0.0; }
    }

    //Carga el archivo CSV(IMBD TOP 1000) y retorna una lista de peliculas
    //Path, ruta al archivo CSV
    //throws, si hay error en lectura del archivo
    public static ArrayList<Movie> loadIMDBTop1000(Path csvPath) {
        ArrayList<Movie> listaMovies = new ArrayList<>();

        //Verifica si el archivo existe
        if (!Files.exists(csvPath)) {
            System.out.println("No se encontro el archivo " + csvPath);
            //Se retorna la lista vacia
            return listaMovies;
        }

        //Si existe se abre con un BufferReader
        else{
            try(BufferedReader br = Files.newBufferedReader(csvPath)){
                //Se lee y se descarta la primera linea que es la de encabezados
                String encabezados = br.readLine();
                String line;

                //Se recorre el archivo linea por linea hasta que no hayan mas
                while ((line = br.readLine()) != null) {
                    //Linea vacia se salta
                    if((line.isBlank())){continue;}

                    //Se divide por comas que no estan en comillas con el patron split
                    //Se usa clean para quitar espacios y comillas externas
                    String[] tokens = SPLIT.split(line, -1);

                    //Extraer campos segun el indice del CSV
                    //Indices: series-title(1), released-year(2), genre(5), imdb-rating(6), director(9)

                    String title;
                    if (tokens.length > 1) {title = clean(tokens[1]);}
                    else {title = "";}

                    String yearStr;
                    if (tokens.length > 2) {yearStr = clean(tokens[2]);}
                    else {yearStr = "";}

                    String genre;
                    if (tokens.length > 5) {genre = clean(tokens[5]);}
                    else {genre = "";}

                    String ratingS;
                    if (tokens.length > 6) {ratingS = clean(tokens[6]);}
                    else {ratingS = "";}

                    String director;
                    if (tokens.length > 9) {director = clean(tokens[9]);}
                    else {director = "";}

                    //Si la peliucla no tiene titulo no se crea la pelicula
                    if(title == null || title.isEmpty()){continue;}
                    int year=cadenaaYear(yearStr);
                    double rating=textoaDouble(ratingS);

                    //Se crea el objeto movie y se agraga a la lista
                    listaMovies.add(new Movie(title, director, genre, year, rating));
                }
        }
            catch(IOException e){
                System.out.println("Error al leer el archivo " + csvPath);
            }
        }
        return listaMovies;
    }
}

//CLASE MOVIE
class Movie{
    //Atributos clase Movie
    private String title; // Titulo de la pelicula
    private String director; // Nombre del director de la pelicula
    private String genre; // Genero o categoria de la pelicula
    private int releaseYear; // Año de lanzamiento de la pelicula
    private double rating; // Clasificacion promedio de la pelicula (entre 0 y 10)

    public static final double EPS = 0.0001; //Tolerancia para comparar doubles

    //Metodos clase Movie

    //Constructor
    public Movie(String title, String director, String genre, int releaseYear, double rating){
        this.title = title;
        this.director = director;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.rating = rating;
    }

    //Setters y Getters
    String setTitle(){return title;}
    String setDirector(){return director;}
    String setGenre(){return genre;}
    int setReleaseYear(){return releaseYear;}
    double setRating(){return rating;}


    public String getTitle(){return title;}
    public String getDirector(){return director;}
    public String getGenre(){return genre;}
    public int getReleaseYear(){return releaseYear;}
    public double getRating(){return rating;}

    //Compara doubles con tolerancia
    public static boolean doubleEquals(double a, double b){
        return Math.abs(a - b) <= EPS;
    }

    //Para imprimir
    public String toString() {
        return
                "Pelicula:\n" + "Titulo: " + title + "\n" + "Director: " + director + "\n" + "Genero = " + genre + "\n" + "Año de estreno: " + releaseYear + "\n" + "Calificación: " + rating + "\n";
    }
}
//========================================================================================================================================================================================================
//CLASE MOVIE CATALOG
class MovieCatalog {
    //Atributos clase MovieCatalog
    private ArrayList<Movie> movies; // Lista de peliculas
    private String sortedByAttribute; //Atributo por el cual esta ordenado la lista

    //Metodos clase MovieCatalog

    //Constructor
    public MovieCatalog(ArrayList<Movie> movies) {
        //Si "movies" es nulo, creo una lista vacia
        if (movies == null) {
            this.movies = new ArrayList<>();
        }

        //En caso contrario, se realiza una copia de la original
        else {
            this.movies = new ArrayList<>(movies);
        }

        //Inicializacion del sorteo en nulo
        this.sortedByAttribute = null;
    }

    //Tamaño de la lista
    public int size() {
        return movies.size();
    }

    //Verifica si una lista esta vacia o no
    public boolean isEmpty() {
        return movies.isEmpty();
    }

    //Getter
    public String getSortedByAttribute() {
        return sortedByAttribute;
    }

    //Para agregar peliculas
    public void agregarPelicula(Movie m) {
        //Agrego la pelicula nueva
        movies.add(m);

        //Al agregar el orden se pierde por lo que marco como no ordenado
        sortedByAttribute = null;
    }

    // Normaliza el nombre del atributo a uno válido (Normalizar: convertirlo a un formato estandar)
    private String actualizarAtributo(String cualidad) {
        //Por defecto retorna rating (indicacion de la guia)
        if (cualidad == null) return "rating";

        //Elimina espacios y deja la palabra en minuscula (trim = elimina espacios/ toLowerCase= deja las letras en minusculas)
        String a = cualidad.trim().toLowerCase();

        //Equals(Metodo que compara si dos objetos son iguales)
        if (a.equals("rating") || a.equals("genre") || a.equals("director") || a.equals("year")) {
            return a;
        }

        return "rating";
    }

    //Comparadores por atributo
    private Comparator<Movie> comparadorPor(String atributo) {
        switch (atributo) {

            //Orden natural de menor a mayor de rating (double)
            case "rating":
                return Comparator.comparingDouble(Movie::getRating);

            //Orden natural de texto (ignora mayuscula y minuscula)
            case "genre":
                return Comparator.comparing(m -> m.getGenre().toLowerCase());

            //Orden natural de texto (ignora mayuscula y minuscula)
            case "director":
                return Comparator.comparing(m -> m.getDirector().toLowerCase());

            //Orden natural de menor a mayor de año
            case "year":
                return Comparator.comparingInt(Movie::getReleaseYear);

            //Orden por defecto de Rating(solicitado en la guia)
            default:
                return Comparator.comparingDouble(Movie::getRating);
        }
    }

    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Ordena el catalogo con base a un algoritmo y un atributo
    public void sortByAlgorithm(String algorithm, String attribute) {
        //Normaliza el atributo escogido (rating, genre, director, año )
        String atributo = actualizarAtributo(attribute);
        String algoritmo;

        if (algorithm == null) {
            algoritmo = " ";
        } else {
            algoritmo = algorithm.trim().toLowerCase();
        }

        //Compara segun ese atributo en especifico
        Comparator<Movie> comparador = comparadorPor(atributo);

        switch (algoritmo) {
            case "insertion":
                insertionSort(comparador);
                break;
            case "selection":
                selectionSort(comparador);
                break;
            case "merge":
                mergeSort(comparador);
                break;
            case "quick":
                quickSort(comparador);
                break;
            case "radix":
                if("year".equals(atributo)) {
                    radixSortYear();
                }
                else{
                    mergeSort(comparador);
                }
                break;
            default:
                Collections.sort(movies, comparador);
        }
        sortedByAttribute = atributo;
    }
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Insertion Sort
    public void insertionSort(Comparator<Movie> comparador) {
        for (int i = 1; i < movies.size(); i++) {
            Movie clave = movies.get(i);
            int j = i - 1;

            // Mueve elementos que sean mayores que 'clave' hacia la derecha
            while (j >= 0 && comparador.compare(movies.get(j), clave) > 0) {
                movies.set(j + 1, movies.get(j));
                j--;
            }
            // Inserta la clave en la posición correcta
            movies.set(j + 1, clave);
        }
    }
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Selection Sort
    public void selectionSort(Comparator<Movie> comparador) {
        for (int i = 1; i < movies.size(); i++) {
            //Indice minimo
            int indiceMin = i;
            for (int j = i + 1; j < movies.size(); j++) {
                if (comparador.compare(movies.get(j), movies.get(indiceMin)) < 0) {
                    indiceMin = j;
                }
            }
            if (indiceMin != i) {
                Movie aux = movies.get(i);
                movies.set(i, movies.get(indiceMin));
                movies.set(indiceMin, aux);
            }
        }
    }
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Merge Sort
    public void mergeSort(Comparator<Movie> comparador) {
        if (movies.size() <= 1) {return;}
        ArrayList<Movie> aux = new ArrayList<>(movies);
        mergeSortRec(0, movies.size()-1, aux, comparador);
    }

    private void mergeSortRec(int lo, int hi, ArrayList<Movie> aux, Comparator<Movie> comparador) {
        if(lo >= hi) {return;}
        int mid = lo + (hi - lo) / 2;
        mergeSortRec(lo, mid, aux, comparador);
        mergeSortRec(mid + 1, hi, aux, comparador);
        merge(lo, mid, hi, aux, comparador);
    }

    private void merge(int lo, int mid, int hi, ArrayList<Movie> aux, Comparator<Movie> comparador) {
        for(int t = lo; t <= hi; t++) {
            aux.set(t, aux.get(t));
        }
        int i = lo;
        int j = lo;
        int k = mid +1;

        while(j <= mid && k <= hi) {
            if (comparador.compare(aux.get(j), aux.get(k)) <= 0) {
                movies.set(i++, aux.get(j++));
            }
            else {
                movies.set(i++, aux.get(k++));
            }
        }
        while(j <= mid) {
            movies.set(i++, aux.get(j++));
        }
    }
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Quick Sort
    public void quickSort(Comparator<Movie> comparador) {
        if (movies.size() <= 1) {return;}

        Collections.shuffle(movies);
        quickRec(0, movies.size() - 1, comparador);
    }

    private void quickRec(int lo, int hi, Comparator<Movie> comparador) {
        if (lo >= hi) {return;}
        int p = particionar(lo, hi, comparador);
        quickRec(lo, p - 1, comparador);
        quickRec(p + 1, hi, comparador);
    }

    private int particionar(int lo, int hi, Comparator<Movie> comparador) {
        Movie pivot = movies.get(hi);
        int i = lo;
        for (int j = lo; j < hi; j++) {
            if (comparador.compare(movies.get(j), pivot) <= 0) {
                swap(i, j);
                i++;
            }
        }
        swap(i, hi);
        return i;
    }

    private void swap(int i, int j) {
        if (i == j) return;
        Movie tmp = movies.get(i);
        movies.set(i, movies.get(j));
        movies.set(j, tmp);
    }
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Radix Sort
    public void radixSortYear() {
        if (movies.isEmpty()) return;

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            int y = m.getReleaseYear();
            if (y < min) min = y;
            if (y > max) max = y;
        }
        int ajuste;
        if(min < 0){
            ajuste = -min;
        }
        else{
            ajuste = 0;
        }

        int[] llave = new int[movies.size()];
        for (int i = 0; i < movies.size(); i++) {
            llave[i] = movies.get(i).getReleaseYear() + ajuste;
        }

        int posicionDigito = 1;
        ArrayList<Movie> temporal = new ArrayList<>(Collections.nCopies(movies.size(), (Movie) null));
        while ((max + ajuste) / posicionDigito > 0) {
            int[] contador = new int[10];

            for (int k : llave) {contador[(k / posicionDigito) % 10]++;}

            for (int i = 1; i < 10; i++) {contador[i] += contador[i - 1];}

            for (int i = movies.size() - 1; i >= 0; i--) {
                int digit = (llave[i] / posicionDigito) % 10;
                int pos = --contador[digit];
                temporal.set(pos, movies.get(i));
            }

            for (int i = 0; i < movies.size(); i++) {
                movies.set(i, temporal.get(i));
                llave[i] = movies.get(i).getReleaseYear() + ajuste;
            }
            posicionDigito *= 10;
        }
    }

    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Lista de peliculas en la cual su calificacion es igual al parametro entregado (con EPS)
    public ArrayList<Movie> getMoviesByRating(double rating) {
        if ("rating".equals(sortedByAttribute)) {
            //Busqueda Binaria (necesita el catalogo previamente ordenado por rating ascendente)
            return buscarPorRatingBinaria(rating);
        } else {
            //Busqueda lineal
            ArrayList<Movie> listaRating = new ArrayList<>();
            for (int i = 0; i < movies.size(); i++) {
                Movie m = movies.get(i);
                if (Movie.doubleEquals(m.getRating(), rating)) {
                    listaRating.add(m);
                }
            }
            return listaRating;
        }
    }

    //Busqueda Binaria para rating (lista ordenada previamente)
    private ArrayList<Movie> buscarPorRatingBinaria(double rating) {
        ArrayList<Movie> listaRating = new ArrayList<>();
        if (movies.isEmpty()) {
            return listaRating;
        }

        int lo = limiteInferiorRating(rating);
        int hi = limiteSuperiorRating(rating) - 1;

        if (lo <= hi && lo >= 0 && hi < movies.size()) {
            for (int i = lo; i <= hi; i++) {
                listaRating.add(movies.get(i));
            }
        }
        return listaRating;
    }

    // Primer índice con rating >= (rating - EPS)
    private int limiteInferiorRating(double rating) {
        double objetivo = rating - Movie.EPS;
        //Comienzo
        int l = 0;

        //Final
        int rr = movies.size();

        //Mientras l sea menor que rr entra al ciclo
        while (l < rr) {
            //Se define la mitad
            int mid = l + (rr - l) / 2;
            double valor = movies.get(mid).getRating();
            if (valor < objetivo) {
                l = mid + 1;
            } else {
                rr = mid;
            }
        }
        return l;
    }

    // Primer índice con rating > (r + EPS)
    private int limiteSuperiorRating(double rating) {
        double objetivo = rating + Movie.EPS;
        int l = 0;
        int rr = movies.size();
        while (l < rr) {
            int mid = l + (rr - l) / 2;
            double val = movies.get(mid).getRating();
            if (val <= objetivo) l = mid + 1;
            else rr = mid;
        }
        return l;
    }

    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    // Lista de películas cuya calificación está dentro del rango entregado
    public ArrayList<Movie> getMoviesByRatingRange(double lowerRating, double higherRating) {
        ArrayList<Movie> listaRango = new ArrayList<>();

        // Caso 1(lista vacia o el rango es invalido)
        if (movies.isEmpty() || lowerRating > higherRating) {
            return listaRango;
        }

        // Si se esta ordenado por rating, se prioriza busqueda binaria (+eficiente)
        if ("rating".equals(sortedByAttribute)) {
            //Comienzo
            int lo = limiteInferiorRating(lowerRating);
            //Final
            int hi = limiteSuperiorRating(higherRating) - 1;

            for (int i = lo; i <= hi && i < movies.size(); i++) {
                double rat = movies.get(i).getRating();
                if (rat + Movie.EPS >= lowerRating && rat - Movie.EPS <= higherRating) {
                    listaRango.add(movies.get(i));
                }
            }
        } else {
            // Si no esta ordenado se usa busqueda lineal
            for (int i = 0; i < movies.size(); i++) {
                Movie m = movies.get(i);
                double rat = m.getRating();
                if (rat + Movie.EPS >= lowerRating && rat - Movie.EPS <= higherRating) {
                    listaRango.add(m);
                }
            }
        }
        return listaRango;
    }

    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Lista de peliculas que sean exactamente del mismo genero que el parametro
    public ArrayList<Movie> getMoviesByGenre(String genre) {
        ArrayList<Movie> listaGenero = new ArrayList<>();

        //Si "movies" es nulo o esta vacio o el director es nulo retorna la lista vacia
        if (movies == null || movies.isEmpty() || genre == null) {
            return listaGenero;
        }

        String generoBuscado = genre.trim();

        if ("genre".equals(sortedByAttribute)) {
            int first = primerIndiceGenero(generoBuscado);
            if (first == -1) {
                return listaGenero;
            }

            int last = ultimoIndiceGenero(generoBuscado);
            for (int i = first; i <= last; i++) {
                listaGenero.add(movies.get(i));
            }
        } else {
            //Si no esta ordenado se usa busqueda lineal
            for (int i = 0; i < movies.size(); i++) {
                Movie m = movies.get(i);
                if (m.getGenre() != null && m.getGenre().equalsIgnoreCase(generoBuscado)) {
                    listaGenero.add(m);
                }
            }
        }
        return listaGenero;
    }

    private int primerIndiceGenero(String objetivo) {
        //Comienzo
        int lo = 0;
        //Final
        int hi = movies.size() - 1;
        //Respuesta primero
        int respuestaPri = -1;

        //Mientras lo sea menor que hi
        while (lo <= hi) {
            //Se define la mitad
            int mid = lo + (hi - lo) / 2;

            String gen = movies.get(mid).getGenre();

            int comparadorGenero = compareGenero(gen, objetivo);

            //Se mueve a la izquierda para encontrar el primero
            if (comparadorGenero >= 0) {
                if (comparadorGenero == 0) {
                    respuestaPri = mid;
                    hi = mid - 1;
                }
            } else {
                lo = mid + 1;
            }
        }
        return respuestaPri;
    }

    private int ultimoIndiceGenero(String objetivo) {
        //Comienzo
        int lo = 0;
        //Final
        int hi = movies.size() - 1;
        //REspuesta ultimo
        int respuestaUlt = -1;

        //Mientras lo sea menor o igual que hi
        while (lo <= hi) {
            //Se define la mitad
            int mid = lo + (hi - lo) / 2;

            String gen = movies.get(mid).getGenre();

            int comparadorGenero = compareGenero(gen, objetivo);

            //Se mueve a la derecha para encontrar el ultimo
            if (comparadorGenero <= 0) {
                if (comparadorGenero == 0) {
                    respuestaUlt = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
        }
        return respuestaUlt;
    }

    // Comparador de director
    //Metodo que ve si una cadena es menor, mayor o igual que otra
    private int compareGenero(String a, String b) {
        //Si son iguales retorna 0
        if (a == null && b == null) {
            return 0;
        }
        //La primera cadena es menor que la segunda
        if (a == null) {
            return -1;
        }
        //La primera cadena es mayor que la segunda
        if (b == null) {
            return 1;
        }
        return a.compareToIgnoreCase(b);
    }

    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Lista de peliculas dirigidas por el director del parametro
    public ArrayList<Movie> getMoviesByDirector(String director) {
        ArrayList<Movie> listaDirectores = new ArrayList<>();
        //Si "movies" es nulo o esta vacio o el director es nulo retorna la lista vacia
        if (movies == null || movies.isEmpty() || director == null) {
            return listaDirectores;
        }

        String directorBuscado = director.trim();

        if ("director".equals(sortedByAttribute)) {

            int first = primerIndice(directorBuscado);
            if (first == -1) {
                return listaDirectores;
            }

            int last = ultimoIndice(directorBuscado);
            for (int i = first; i <= last; i++) {
                listaDirectores.add(movies.get(i));
            }

        } else {
            // Si no esta ordenado se usa busqueda lineal
            for (int i = 0; i < movies.size(); i++) {
                Movie m = movies.get(i);
                if (m.getDirector() != null && m.getDirector().equalsIgnoreCase(directorBuscado)) {
                    listaDirectores.add(m);
                }
            }
        }
        return listaDirectores;
    }

    // Devuelve el primer índice
    private int primerIndice(String objetivo) {
        //Comienzo
        int lo = 0;
        //Final
        int hi = movies.size() - 1;
        //Respuesta del primero
        int respuestaPri = -1;

        //Mientras lo sea menor que hi
        while (lo <= hi) {
            //Se define la mitad
            int mid = lo + (hi - lo) / 2;

            String dir = movies.get(mid).getDirector();

            int comparadorDirector = compareDirector(dir, objetivo);
            //Se mueve a la izquierda para encontrar el primer indice
            if (comparadorDirector >= 0) {
                if (comparadorDirector == 0) {
                    respuestaPri = mid;
                    hi = mid - 1;
                }
            }
            //Se mueve a la derecha para encontrar el primer indice
            else {
                lo = mid + 1;
            }
        }
        return respuestaPri;
    }

    // Devuelve el ultimo índice
    private int ultimoIndice(String objetivo) {
        //Comienzo
        int lo = 0;
        //Final
        int hi = movies.size() - 1;
        //Respuesta del ultimo
        int respuestaUlt = -1;

        //Mientras lo sea menor que hi
        while (lo <= hi) {
            //Se define la mitad
            int mid = lo + (hi - lo) / 2;

            String dir = movies.get(mid).getDirector();

            int comparadorDirector = compareDirector(dir, objetivo);

            //Se mueve a la derecha para encontrar el ultimo
            if (comparadorDirector <= 0) {
                if (comparadorDirector == 0) {
                    respuestaUlt = mid;
                    lo = mid + 1;
                }

            }
            //Se mueve a la derecha para encontrar el primer indice
            else {
                hi = mid - 1;
            }
        }
        return respuestaUlt;
    }

    // Comparador de director
    //Metodo que ve si una cadena es menor, mayor o igual que otra
    private int compareDirector(String a, String b) {
        //Si son iguales retorna 0
        if (a == null && b == null) {
            return 0;
        }
        //La primera cadena es menor que la segunda
        if (a == null) {
            return -1;
        }
        //La primera cadena es mayor que la segunda
        if (b == null) {
            return 1;
        }
        return a.compareToIgnoreCase(b);
    }

    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //========================================================================================================================================================================================================
    //Lista de peliculas estrenadas en el año del parametro
    public ArrayList<Movie> getMoviesByYear(int year) {
        ArrayList<Movie> listaYear = new ArrayList<>();
        //Si "movies" es nulo o esta vacio o el director es nulo retorna la lista vacia
        if (movies == null || movies.isEmpty()) {
            return listaYear;
        }

        if ("year".equals(sortedByAttribute)) {

            int first = primerIndiceYear(year);
            if (first == -1) {
                return listaYear;
            }

            int last = ultimoIndiceYear(year);
            for (int i = first; i <= last; i++) {
                listaYear.add(movies.get(i));
            }
        } else {
            // Si no esta ordenado se usa busqueda lineal
            for (int i = 0; i < movies.size(); i++) {
                Movie m = movies.get(i);
                if (m.getReleaseYear() == year) {
                    listaYear.add(m);
                }
            }
        }
        return listaYear;
    }

    // Devuelve el primer índice
    private int primerIndiceYear(int objetivo) {
        //Comienzo
        int lo = 0;
        //Final
        int hi = movies.size() - 1;
        //Respuesta primero
        int respuestaPri = -1;

        //Mientras lo sea menor o igual a hi
        while (lo <= hi) {
            //Se define la mitad
            int mid = lo + (hi - lo) / 2;

            int ye = movies.get(mid).getReleaseYear();

            int comparadorYear = compareYear(ye, objetivo);
            if (comparadorYear >= 0) {           // explorar a la izquierda para hallar el primero
                if (comparadorYear == 0) {
                    respuestaPri = mid;
                    hi = mid - 1;
                }
            } else {
                lo = mid + 1;
            }
        }
        return respuestaPri;
    }

    // Devuelve el ultimo indice
    private int ultimoIndiceYear(int objetivo) {
        //Comienzo
        int lo = 0;
        //Final
        int hi = movies.size() - 1;
        //Respuesta ultimo
        int respuestaUlt = -1;

        //Mientras lo sea menor o igual a hi
        while (lo <= hi) {
            //Se define la mitad
            int mid = lo + (hi - lo) / 2;

            int ye = movies.get(mid).getReleaseYear();

            int comparadorYear = compareYear(ye, objetivo);
            if (comparadorYear <= 0) {           // explorar a la derecha para hallar el último
                if (comparadorYear == 0) {
                    respuestaUlt = mid;
                    lo = mid + 1;
                }
            } else {
                hi = mid - 1;
            }
        }
        return respuestaUlt;
    }

    //Comparador para el año
    private int compareYear(int a, int b) {
        return Integer.compare(a, b);
    }
}
//========================================================================================================================================================================================================
//CLASE MAIN
public class Main {
    public static void main(String[] args) {
        Path csvPath = Paths.get("C:/Users/cinth/IdeaProjects/Laboratorio_3_EDA_CI/src/imdb_top_1000.csv");
        ArrayList<Movie> allMovies = CsvIMDBLoader.loadIMDBTop1000(csvPath);
        System.out.println("Películas cargadas: " + allMovies.size());

        //Experimento 1A: analisis de algoritmos de ordenamiento
        System.out.println();
        System.out.println("Experimento 1A: Análisis de Algoritmos de Ordenamiento");

        //Emcabezados para la tabla, tiempo medido en nanosegundos (ns)
        System.out.println();
        System.out.printf("%-20s %-20s %-20s %-20s%n", "Tamaño", "InsertionSort [ns]", "MergeSort [ns]", "RadixSort [ns]");

        //Medicion incremental. Mide el tiempo para ordenar subconjuntos(tamaños dados en la guia)
        int[] subconjuntos = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};

        for (int i = 0; i < subconjuntos.length; i++) {
            int n = subconjuntos[i];
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //CASO 1: Sublista con los n primeros elemetos(peli)
            ArrayList<Movie> subLista = new ArrayList<>(allMovies.subList(0, n));

            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //CASO 2: Insertion/Selection Sort
            MovieCatalog catalogo1 = new MovieCatalog(subLista);
            long inicio = System.nanoTime();

            //Aqui puede ser: insertion o selection sort (elegimos InsertionSort)
            catalogo1.insertionSort(Comparator.comparingDouble(Movie::getRating));
            //catalogo1.selectionSort(Comparator.comparingDouble(Movie::getRating));

            long timeInsertion = System.nanoTime() - inicio;

            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //CASO 3: Merge/Quick Sort
            MovieCatalog catalogo2 = new MovieCatalog(subLista);
            inicio = System.nanoTime();

            //Aqui puede ser: merge o quick sort(elegimos MergeSort)
            catalogo2.mergeSort(Comparator.comparingDouble(Movie::getRating));
            //catalogo2.quickSort(Comparator.comparingDouble(Movie::getRating));

            long timeMerge = System.nanoTime() - inicio;

            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //RadixSort
            MovieCatalog catalogo3 = new MovieCatalog(subLista);
            inicio = System.nanoTime();
            catalogo3.radixSortYear();
            long timeRadix = System.nanoTime() - inicio;

            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //Mostrar la fila de resultados
            System.out.printf("%-20d %-20d %-20d %-20d%n", n, timeInsertion, timeMerge, timeRadix);
        }
        //Experimento 1: analisis de algoritmos de ordenamiento
        System.out.println();
        System.out.println("Experimento 1B: Análisis de Algoritmos de Ordenamiento");

        //Emcabezados para la tabla, tiempo medido en nanosegundos (ns)
        System.out.println();
        System.out.printf("%-20s %-20s %-20s%n", "Tamaño", "SelectionSort [ns]", "QuickSort [ns]");

        //Medicion incremental. Mide el tiempo para ordenar subconjuntos(tamaños dados en la guia)
        int[] subconjuntos2 = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};

        for (int i = 0; i < subconjuntos2.length; i++) {
            int n = subconjuntos2[i];
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //CASO 1: Sublista con los n primeros elemetos(peli)
            ArrayList<Movie> subLista = new ArrayList<>(allMovies.subList(0, n));

            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //CASO 2: Insertion/Selection Sort
            MovieCatalog catalogo1 = new MovieCatalog(subLista);
            long inicio = System.nanoTime();

            //Aqui puede ser: insertion o selection sort (elegimos InsertionSort)
            //catalogo1.insertionSort(Comparator.comparingDouble(Movie::getRating));
            catalogo1.selectionSort(Comparator.comparingDouble(Movie::getRating));

            long timeSelection = System.nanoTime() - inicio;

            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //CASO 3: Merge/Quick Sort
            MovieCatalog catalogo2 = new MovieCatalog(subLista);
            inicio = System.nanoTime();

            //Aqui puede ser: merge o quick sort(elegimos MergeSort)
            //catalogo2.mergeSort(Comparator.comparingDouble(Movie::getRating));
            catalogo2.quickSort(Comparator.comparingDouble(Movie::getRating));

            long timeQuick = System.nanoTime() - inicio;

            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //=======================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //========================================================================================================================================================================================================
            //Mostrar la fila de resultados
            System.out.printf("%-20d %-20d %-20d%n", n, timeSelection, timeQuick);
        }


        //Experimento 2: analisis de algoritmos de busqueda
        System.out.println();
        System.out.println("Experimento 2: Búsqueda Lineal vs Binaria (promedio sobre 5 directores)");

        //Emcabezados para la tabla
        System.out.println();
        System.out.printf("%-15s %-30s %-30s%n", "n", "Lineal promedio [ns]", "Binaria promedio [ns]");
        final int REPETICIONES = 50000;

        for (int idx = 0; idx < subconjuntos.length; idx++) {
            int n = subconjuntos[idx];

            if (allMovies.size() < n) {
                break;
            }

            //Se crea un catalogo de tamaño n
            ArrayList<Movie> sublista = new ArrayList<>(allMovies.subList(0, n));

            //Eleccion de 5 directores presentes en la sublista
            ArrayList<String> directores = pickDirectorsFrom(sublista, 5);
            int usados = directores.size();

            if(usados < 5){
               continue;
            }


            //BÚSQUEDA LINEAL: catálogo desordenado
            //Sin ordenar por director
            MovieCatalog catLineal = new MovieCatalog(sublista);
            long totalLineal = 0L;

            for (int d = 0; d < directores.size(); d++) {
                String objetivo = directores.get(d);
                long inicio = System.nanoTime();

                //Se repite varias veces para estabilizar medida
                for (int r = 0; r < REPETICIONES; r++) {
                    // getMoviesByDirector usa lineal sino esta ordenado
                    catLineal.getMoviesByDirector(objetivo);
                }
                long fin = System.nanoTime();
                totalLineal += (fin - inicio);
            }

            long promedioLineal = totalLineal / directores.size();

            //BÚSQUEDA BINARIA: catalogo previamente ordenado por director
            MovieCatalog catBinaria = new MovieCatalog(sublista);

            //Con Comparator
            catBinaria.mergeSort(Comparator.comparing(Movie::getDirector));

            // Opción B (si usas interfaz propia): catBinaria.sortByAlgorithm("merge", "director");
            long totalBinaria = 0L;
            for (int d = 0; d < directores.size(); d++) {
                String objetivo = directores.get(d);
                long inicio = System.nanoTime();


                for (int r = 0; r < REPETICIONES; r++) {
                    // getMoviesByDirector usa busqueda binaria si YA está ordenado por "director"
                    catBinaria.getMoviesByDirector(objetivo);
                }
                long fin = System.nanoTime();
                totalBinaria += (fin - inicio);
            }

            long promedioBinaria = totalBinaria / directores.size();

            //Registro de promedios
            System.out.printf("%-15d %-30d %-30d%n", n, promedioLineal, promedioBinaria);
        }
    }
    //
    private static ArrayList<String> pickDirectorsFrom(List<Movie> list, int k) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (int i = 0; i < list.size(); i++) {
            String dir = list.get(i).getDirector();
            if (dir != null && !dir.isBlank() && !"Unknown".equalsIgnoreCase(dir)) {
                set.add(dir);
                if (set.size() == k) break;
            }
        }
        return new ArrayList<>(set);
    }
}

