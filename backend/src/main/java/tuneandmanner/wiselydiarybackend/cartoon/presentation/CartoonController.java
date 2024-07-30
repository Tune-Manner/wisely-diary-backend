package tuneandmanner.wiselydiarybackend.cartoon.presentation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.CreateCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.service.CartoonService;

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
}
