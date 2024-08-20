package tuneandmanner.wiselydiarybackend.cartoon.presentation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.CreateCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.CreateLetterCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.SaveCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.response.InquiryCartoonResponse;
import tuneandmanner.wiselydiarybackend.cartoon.service.CartoonService;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/cartoon")
@RestController
public class CartoonController {

    private final CartoonService cartoonService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    // 만화생성과 편지 만화 생성 동시 실행
    @PostMapping("/create")
    public ResponseEntity<String> createCartoonAndLetter(@RequestBody CreateCartoonRequest request) throws ExecutionException, InterruptedException {
        log.info("CartoonController.createCartoonAndLetter");

        CompletableFuture<String> cartoonFuture = CompletableFuture.supplyAsync(
                () -> cartoonService.createCartoonPrompt(request), executorService
        );

        CompletableFuture<String> letterCartoonFuture = CompletableFuture.supplyAsync(
                () -> cartoonService.createLetterCartoonPrompt(request), executorService
        );

        // 두 작업이 완료될 때까지 기다린 후 URL을 반환
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(cartoonFuture, letterCartoonFuture);
        combinedFuture.join();

        String cartoonUrl = cartoonFuture.get();
        String letterCartoonUrl = letterCartoonFuture.get();

        String result = "Cartoon URL: " + cartoonUrl + ", Letter Cartoon URL: " + letterCartoonUrl;
        return ResponseEntity.ok(result);
    }

    //만화 저장
    @PostMapping("/save")
    public ResponseEntity<Long> saveCartoon(@RequestBody SaveCartoonRequest request){
        log.info("CartoonController.saveCartoon");
        Long cartoonCode = cartoonService.saveCartoon(request);
        return ResponseEntity.ok(cartoonCode);
    }

    //로그인한 유저의 오늘 생성 된 만화 조회
    @GetMapping("/inquiry")
    public ResponseEntity<List<InquiryCartoonResponse>> getCartoon(
            @RequestParam LocalDate date,
            @RequestParam String memberId
    ){
        List<InquiryCartoonResponse> cartoons = cartoonService.findCartoon(date,memberId);
        System.out.println("4");
        if(cartoons.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else{
            return new ResponseEntity<>(cartoons,HttpStatus.OK);
        }
    }

}
