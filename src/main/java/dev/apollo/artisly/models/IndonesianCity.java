package dev.apollo.artisly.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum IndonesianCity {
    ACEH("Aceh"),
    BALIKPAPAN("Balikpapan"),
    BANDA_ACEH("Banda Aceh"),
    BANDAR_LAMPUNG("Bandar Lampung"),
    BANDUNG("Bandung"),
    BANJARMASIN("Banjarmasin"),
    BATAM("Batam"),
    BEKASI("Bekasi"),
    BENGKULU("Bengkulu"),
    BIMA("Bima"),
    BINJAI("Binjai"),
    BITUNG("Bitung"),
    BLITAR("Blitar"),
    BOGOR("Bogor"),
    BONTANG("Bontang"),
    CILEGON("Cilegon"),
    CIMAHI("Cimahi"),
    CIREBON("Cirebon"),
    DENPASAR("Denpasar"),
    DEPOK("Depok"),
    DUMAI("Dumai"),
    GORONTALO("Gorontalo"),
    JAKARTA("Jakarta"),
    JAMBI("Jambi"),
    JAYAPURA("Jayapura"),
    JEMBER("Jember"),
    JEPARA("Jepara"),
    KEDIRI("Kediri"),
    KENDARI("Kendari"),
    KUPANG("Kupang"),
    KUTAI_BARAT("Kutai Barat"),
    KUTAI_KARTANEGARA("Kutai Kartanegara"),
    LANGSA("Langsa"),
    LUBUKLINGGAU("Lubuklinggau"),
    MADIUN("Madiun"),
    MAGELANG("Magelang"),
    MAKASSAR("Makassar"),
    MALANG("Malang"),
    MANADO("Manado"),
    MATARAM("Mataram"),
    MEDAN("Medan"),
    METRO("Metro"),
    MOJOKERTO("Mojokerto"),
    PADANG("Padang"),
    PADANG_PANJANG("Padang Panjang"),
    PAGARALAM("Pagaralam"),
    PALEMBANG("Palembang"),
    PALOPO("Palopo"),
    PALU("Palu"),
    PANGKAL_PINANG("Pangkal Pinang"),
    PAREPARE("Parepare"),
    PARIAMAN("Pariaman"),
    PASURUAN("Pasuruan"),
    PONTIANAK("Pontianak"),
    PRABUMULIH("Prabumulih"),
    PROBOLINGGO("Probolinggo"),
    SABANG("Sabang"),
    SALATIGA("Salatiga"),
    SAMARINDA("Samarinda"),
    SAWAHLUNTO("Sawah Lunto"),
    SEMARANG("Semarang"),
    SERANG("Serang"),
    SIBOLGA("Sibolga"),
    SINGKAWANG("Singkawang"),
    SOLOK("Solok"),
    SORONG("Sorong"),
    SUBULUSSALAM("Subulussalam"),
    SUKABUMI("Sukabumi"),
    SUNGAI_PENUH("Sungai Penuh"),
    SURABAYA("Surabaya"),
    SURAKARTA("Surakarta"),
    TANGERANG("Tangerang"),
    TANGERANG_SELATAN("Tangerang Selatan"),
    TANJUNG_BALAI("Tanjung Balai"),
    TARAKAN("Tarakan"),
    TASIKMALAYA("Tasikmalaya"),
    TEBING_TINGGI("Tebing Tinggi"),
    TEGAL("Tegal"),
    TERNATE("Ternate"),
    TIDORE_KEPULAUAN("Tidore Kepulauan"),
    TOMOHON("Tomohon"),
    TRENGGALEK("Trenggalek"),
    TUAL("Tual"),
    TUBAN("Tuban"),
    TULUNGAGUNG("Tulungagung"),
    YOGYAKARTA("Yogyakarta");

    private final String name;

    IndonesianCity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static List<String> getList() {
        List<String> cities = Arrays.stream(IndonesianCity.values()).map(IndonesianCity::getName).collect(Collectors.toList());
        cities.replaceAll(String::toUpperCase);
        return cities;
    }


}
