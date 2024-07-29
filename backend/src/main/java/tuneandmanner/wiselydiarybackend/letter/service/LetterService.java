package tuneandmanner.wiselydiarybackend.letter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tuneandmanner.wiselydiarybackend.letter.domain.repository.LetterRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;

}
