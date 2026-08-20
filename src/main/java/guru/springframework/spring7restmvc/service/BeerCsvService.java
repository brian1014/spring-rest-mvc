package guru.springframework.spring7restmvc.service;

import guru.springframework.spring7restmvc.model.BeerCSVRecord;

import java.io.File;
import java.util.List;

public interface BeerCsvService {
    List<BeerCSVRecord> convertCSV(File file);
}
