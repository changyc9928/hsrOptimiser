package com.hsrOptimiser.services;

import com.hsrOptimiser.domain.hsrScanner.ScannedData;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;

public interface DataPopulatingService {

    PopulatedData populateData(ScannedData scannedData);
}
