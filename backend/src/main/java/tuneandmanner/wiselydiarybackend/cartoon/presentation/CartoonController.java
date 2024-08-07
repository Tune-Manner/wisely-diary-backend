package tuneandmanner.wiselydiarybackend.cartoon.presentation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.CreateCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.SaveCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.response.InquiryCartoonResponse;
import tuneandmanner.wiselydiarybackend.cartoon.service.CartoonService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/cartoon")
@RestController
public class CartoonController {

    private final CartoonService cartoonService;

    // 만화생성
    @PostMapping("/create")
    public ResponseEntity<String> createCartoon(@RequestBody CreateCartoonRequest request){
        log.info("CartoonController.createCartoon");

        String cartoonUrl = cartoonService.createCartoonPrompt(request);
        return ResponseEntity.ok(cartoonUrl);
    }

    //만화 저장
    @PostMapping("/save")
    public ResponseEntity<Integer> saveCartoon(@RequestBody SaveCartoonRequest request){
        log.info("CartoonController.saveCartoon");
        Integer cartoonCode = cartoonService.saveCartoon(request);
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
