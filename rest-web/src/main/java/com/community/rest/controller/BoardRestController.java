package com.community.rest.controller;

import com.community.rest.domain.Board;
import com.community.rest.repository.BoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/boards")
public class BoardRestController {

    private BoardRepository boardRepository;

    //Autowired 와 똑같이 의존성을 주입하는 생성자 주입 방식
    public BoardRestController(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    //Get으로 '/api/boards' 호출시 해당 메서드에 매핑. 반환값은 JSON타입
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoards(@PageableDefault Pageable pageable) {
        Page<Board> boards = boardRepository.findAll(pageable);
        //현재 페이지 수, 총 게시판 수, 한 페이지의 게시판 수 등 페이징 처리에 관한 리소스를 만드는 PageResoures 객체를 생성하기 위해
        //PageResoures 생성자의 파라미터로 사용되는 PageMetadata 객체를 생성. PageMetadata는 전체페이지 수, 편재 페이지 번호, 총 게시판 수로 구성
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageable.getPageSize(), boards.getNumber(), boards.getTotalElements());
        //PageResources 객체를 생성. 이 객체를 생성 하면 HATEOAS가 적용되며 페이징값까지 생성된 REST형의 데이터를 만든다.
        PagedResources<Board> resources = new PagedResources<>(boards.getContent(), pageMetadata);
        //PageResources 객체 생성시 따로 링크를 설정하지 않았다면 이와 같이 링크 추가 가능. 여기서는 각 Board마다 상세정보를 불러올 수 있는 링크만 추가
        resources.add(linkTo(methodOn(BoardRestController.class).getBoards(pageable)).withSelfRel());
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<?> postBoard(@RequestBody Board board) {
        board.setCreatedDate();
        boardRepository.save(board);
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    @PutMapping("/{idx}")
    public ResponseEntity<?> putBoard(@PathVariable("idx")Long idx, @RequestBody Board board) {
        Board persistBoard = boardRepository.getOne(idx);
        persistBoard.update(board);
        boardRepository.save(persistBoard);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @DeleteMapping("/{idx}")
    public ResponseEntity<?> deleteBoard(@PathVariable("idx")Long idx) {
        boardRepository.deleteById(idx);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
