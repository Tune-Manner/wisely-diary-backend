package tuneandmanner.wiselydiarybackend.letter.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuneandmanner.wiselydiarybackend.letter.service.LetterService;

@Slf4j
@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

}
