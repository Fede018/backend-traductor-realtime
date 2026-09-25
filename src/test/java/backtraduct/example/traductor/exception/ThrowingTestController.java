package backtraduct.example.traductor.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ThrowingTestController {

	@GetMapping("/test/error")
	public String throwError() {
		throw new RuntimeException("boom");
	}
}
