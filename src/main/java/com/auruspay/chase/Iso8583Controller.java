package com.auruspay.chase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.auruspay.chase.service.ChaseValidationService;
import com.auruspay.chase.validation.Iso8583FieldValidationResult;
import com.auruspay.chase.validation.Iso8583FieldValidator;

/**
 * Example wiring only - adjust request/response shape, error handling, and
 * path to fit your actual API contract. Not meant to be used as-is; shows
 * how the two services above plug into a controller.
 */
@RestController
public class Iso8583Controller {

    private  final  Iso8583MessageParser parser;
    private  final   ChaseValidationService chaseValidationService;
    private final Iso8583FieldValidator validator;


     Iso8583Controller(Iso8583MessageParser parser ,  ChaseValidationService chaseValidationService,Iso8583FieldValidator validator) {
        this.parser = parser;
        this. chaseValidationService =    chaseValidationService ;
        this.validator =  validator;
        
    }

    @GetMapping("/chase/parse")
    public ResponseEntity<?> parse() {
        try {
        	  String aaprovedRequest = "UKCNJ+XZuBBEynMbS6dqsnsXj3vUQUua/u9AV2iNl76Zl5bZaPN6DU4IYXltGY54Qrwufdyw4+Lqbim5rRrCB0QJ32d17pVBlgcTakut8bTM1oMqUyo63W3CbinpVlmG7T3aHJJxm5UEUekc9ZFkA2H9z/oPLwW8DguwavkQkHukorw1a1rj1T+v3FKd0b1ms4E09i6gEd5NjY2RJIz/qrP2ghufMrQZ1V/Jpf5Ad9XSDGC/yv12J8q6n2685AFokFFoy7ARUvj4bbidqJ5YVqI+0H/HvSJ8U56X81HjHRVbah41SqE2NcZw/7tKgV3Swy+NEHMBdTjXEhfAPgFsDKjrhx/uGsWXh5xN29MlRtA+M+YmhdhWQE6tOlT0oY2Nkcj5ohun0HIcXrkw5G+ztudvJcsF5MkfAV9n9pwGaQev4KMREpPSx1S9428ju7XXjGnqhJmj9mqFCp9TyLA060Y6Ryb+TDmURQozjMs8unqYmyBczAmf+aHs7A1w0h9QTMMV5wWh4ixpnVZfMRaEyfeqnbLw+XbQzVm2J6SST2w1qR4Dk6TDM7k1qf9IExiPTG5qDleSVqHjKZadqxSvuhmRujfMczBp93oWRKK6UogGE1nnxIHa8FFsbtwHDdS57pE/FcjYIhzNgcl3NluTb2kNcUN47JqSbBuXthfUlDjCrO6FyO5zhq7vb1OFYzlf23g3VYeA3Qb2xXPbMZTEQwKdGUjCyGCkUajmb40m6xpJrl5iCFTL7bRJKRBQty2jYf3P+g8vBbwOC7Bq+RCQex5x2P0JDUWoQNPQiLRXDBvnGy2Nc9mkSYP/cmDN3+28sXKq5dhGsbu8TNJkzDDuRQ== ";
        	  String declinedRequest = "UKCNJ+XZuBBEynMbS6dqsnsXj3vUQUua/u9AV2iNl76Zl5bZaPN6DU4IYXltGY541EHM0wR54QZD4iTgHr/yZdDPWjKp8CeJ4ibqFcMkCeWSPY7UXAARE1WOKms3z2r2XewfphJVE9X2n4RLQK7rWZqSunJHmVyXa13N7LRH6W0nxOiY9QkYAv+GvN+7Hgltj+3m/Ctho5Oqqy2EvnUWjhx10XfIBfzY4PaoB5XnzUp61DbyBsO43dNdHv4Da+wXjhXFQL/fFUukjMZodouaWpI9jtRcABETVY4qazfPavb5JeMXSe/WlFPtafYbDVgXipZMKWArjhYnGMm9kp1P7NGTXc+m/Vb7CopQj70W6W9vNOfe9Hio6kDTeBbAcez0JTSU3xRM6pF/T088Ow7qwbEBJHYkGJ/B5fXiAvlZ3SeXzqI7iww0Ok5Ytx9aT6OEoRgeaJKxQi++tGytgVFdGRnKtszgsdoo79yPXR29skXiHtIs3XzvAaWAzM8kaYjxHwbFr5y3B2oJozW+moN/PSZ9NH7asP0D5i94xjBG5wFBzLdTX/Hq+vYnfnuY/42JEEpQPHlqqu8MalGlgyX/roa+iZl8GdV9K0RYzXz7AWu3nIL8u4VNl56gDgVTmgNdhOEFZr6E8/j/Kb+oJCi2+LhUOtguq8JfC+xGGs8THDjcrdaeqMLHLOucJboC48jQV3tJzL+CFYBG9kSdM6Gl18BdERIpfnLpaT7kLtlQREb3tRW45cWd2svs1Q248KR4opmDL39apzPu3Yj9BGCo3zvKoqmGFYFvJ2rq0CqnYldZvoS6Oh4G5cSZMdNqpeO0O55DEkh9keGhZvcnycE0W9J7I8XoXoMLOrM3kL3PxnucUbjSFg1+ieghXzVxEEfxYf3P+g8vBbwOC7Bq+RCQe2h+JxoolIS8bTSp/817G4cIU3qEG/xo9GoP/sDIouaRnftBOQXFOf1mJo5oc8ndcpZEKRgqVYiH7TfcNDKP5A4f6joOkCS+7hEmkYje7nHNPRY7kybnAvtFdYZfw5L2aoBHcNYuB1YHCBAhS/sk7zwTbtHIfGjFi350yFCAeWGaQfr/ep/VjhqSV+TJWHjqhG7wJ58sbyVWJPnKD1VN43Tl36y/2yV8r4VyO4qhuhz54ysRF/+OS+HClWZksD6YvITTIn2K1wxVHdIYCi3QrBKrOgNTROxNL4e6cBqdaby5T2rkpleC6rnHO4m9wAZgxEE5eW4Zbsh+dvBFw1Mb7Jwp78WaOsXHASJPy4gEOXLxkLTyg9GGP8kFSupycrNpuvEt9g/kOywtDe4jRyhKdNDvnYRmVa2x/h9mYx4YI40sYf3P+g8vBbwOC7Bq+RCQe1HaX72gpAI0EsMKMCioxZ8VZ6S2rrhWc8bmxu9DPERb+lnMBmpQj1F0uor1zYGCpWH9z/oPLwW8DguwavkQkHth/c/6Dy8FvA4LsGr5EJB7noY+7XGxYzflDZ4D79veq1UPVQsYug9stcs47mF9xvdVsvfFhzAXnHG7k1wKFCUX/2zTXlbJ/U+fV3mo2MONxgcbno4rSXIpQ+dbCIOgp8tuC0epfXPO8I+FuIBu9m5YVzCVnQ+4mP1QCUdWo47EKqSRbIqws2Yjds/682njmje1jPu3Jg2I/mEoLCb9U0WUVgV50CPO21OuPRE9GcbGHNEFVKoX30NWyihffjBAdZEW8bpsHUJRKrp9HTxQ5qMBLRucTHq45n+WQWkJfNmiASI8Ool/UlF10wM2RKhQ0GTbeDdVh4DdBvbFc9sxlMRD23g3VYeA3Qb2xXPbMZTEQ4uRlvzdYsylHjzCtAzMlAR7WC04eh3BIpb0aTiQsGbTDC43OA9hGYJGuQCKhViUTCS09UpmDwNl2M7JBKdwoBOGzw4ES9U/YuG2fjpm6wAg ";
            Iso8583ParseResult approvedRequestParse = parser.parseMessage(aaprovedRequest);
            Iso8583ParseResult declinedRequestParse = parser.parseMessage(aaprovedRequest);
            
           
            
            Map<String , Iso8583ParseResult> request = new HashMap<>();
            request.put("aaprovedRequest", approvedRequestParse);
            request.put("declinedRequest", declinedRequestParse);
            
          List<Iso8583FieldValidationResult> result = chaseValidationService.validation(approvedRequestParse, declinedRequestParse);
          
            return ResponseEntity.ok(result);
        } catch (Iso8583ParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
    

    
