package guru.springframework.spring6restmvc.service;

import guru.springframework.spring6restmvc.model.BeerCSVRecord;

import java.io.File;
import java.util.List;

public interface IBeerCsvService {
    List<BeerCSVRecord> convertCSV(File csvFile);
}
