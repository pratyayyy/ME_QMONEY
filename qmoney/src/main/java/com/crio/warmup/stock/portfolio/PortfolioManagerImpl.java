
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF
  

  

public PortfolioManagerImpl() {}


@Override

public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
  LocalDate endDate) throws JsonProcessingException
  {
    List<AnnualizedReturn> annualizedReturns=new ArrayList<>();
    for(PortfolioTrade trade: portfolioTrades)
    {
      List<Candle> candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
      double buy_price = candles.get(0).getOpen();
      double sell_price= candles.get(candles.size()-1).getClose();
      AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate,
       trade, buy_price, sell_price);
      annualizedReturns.add(annualizedReturn);
    }
    Collections.sort(annualizedReturns, getComparator());
    return annualizedReturns;
  }
 

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
  PortfolioTrade trade, Double buyPrice, Double sellPrice) {

  Double totalReturns = (sellPrice - buyPrice)/buyPrice;
  Double annualizedReturn = Math.pow((1+totalReturns),(1/TimeInYears(trade.getPurchaseDate(),endDate)))-1;
  return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);
}

public static double TimeInYears(LocalDate startDate, LocalDate endDate)
 {
  Double days = (double)ChronoUnit.DAYS.between(startDate,endDate)/365.24;
  return days;
 }




  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.

  
 
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        List<Candle> lt = new ArrayList<Candle>(); 
        RestTemplate restTemplate = new RestTemplate();
        String URL = buildUri(symbol, from,to);
        TiingoCandle[] tc = restTemplate.getForObject(URL, TiingoCandle[].class);

        for(Candle c : tc)
        {
          lt.add(c);
        }
        return lt;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
       String token = "d9cb58e77103b4fae2f494974786d868521388a4";      
            return uriTemplate.replace("$APIKEY",token).replace("$SYMBOL",symbol).replace("$STARTDATE",startDate.toString()).replace("$ENDDATE",endDate.toString());
  }
}
