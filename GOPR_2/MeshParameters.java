package GOPR_2;
/**
 *
 * @author Michal
 */
//Konfiguracja siatki i jej parametrow
public class MeshParameters {
    public int pointCount; //Liczba punktow na bazie ktorych bedzie dzialala triangulacja Delaunaya
    public int accuracy; //Zageszczenie siatki -> im mniejsza wartosc tym bardziej gesta siatka
                         // -> im wieksza wartosc tym mniej gesta bedzie siatka
    public boolean fill; //Wypelnienie kolorem tla bazowej mikrostruktury
}
