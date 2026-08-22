package com.cropportal.ai;

import com.cropportal.dto.TicketRequest;
import com.cropportal.entity.DiseaseReport;
import com.cropportal.entity.Farmer;

public interface TicketAiSupportService {
    String resolveTicket(Farmer farmer, TicketRequest request, DiseaseReport diseaseReport);
}
