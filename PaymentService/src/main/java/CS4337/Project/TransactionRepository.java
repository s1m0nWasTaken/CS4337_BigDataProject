package CS4337.Project;

import CS4337.Project.Shared.Models.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, Integer> {}
