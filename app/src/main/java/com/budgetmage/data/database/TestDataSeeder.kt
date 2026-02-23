package com.budgetmage.data.database

import com.budgetmage.data.database.dao.TransactionDao
import com.budgetmage.data.database.entity.TransactionEntity
import com.budgetmage.data.database.entity.TransactionType
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TestDataSeeder @Inject constructor(
    private val transactionDao: TransactionDao
) {
    private val expenseDescriptions = listOf(
        "Supermercado", "Restaurante", "Lanche", "Café", "Padaria",
        "Uber", "Combustível", "Estacionamento", "Ônibus", "Metrô",
        "Aluguel", "Condomínio", "IPTU", "Manutenção",
        "Luz", "Água", "Internet", "Telefone", "Gás",
        "Farmácia", "Consulta médica", "Exames", "Academia",
        "Cinema", "Netflix", "Spotify", "Jogos", "Livros",
        "Curso online", "Material escolar", "Mensalidade",
        "Presente", "Roupas", "Eletrônicos", "Diversos"
    )

    private val incomeDescriptions = listOf(
        "Salário mensal", "Bônus", "13º salário",
        "Dividendos", "Rendimento poupança", "Rendimento CDB",
        "Projeto freelance", "Consultoria", "Trabalho extra",
        "Reembolso", "Venda", "Presente recebido"
    )

    suspend fun seedTestTransactions(count: Int = 100) {
        val transactions = mutableListOf<TransactionEntity>()
        val today = LocalDate.now()
        val random = Random(System.currentTimeMillis())

        repeat(count) { index ->
            // 70% expenses, 30% income
            val isExpense = random.nextFloat() < 0.7f
            val type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME

            // Category IDs: 1-8 for expenses, 9-12 for income
            val categoryId = if (isExpense) {
                random.nextLong(1, 9) // 1 to 8
            } else {
                random.nextLong(9, 13) // 9 to 12
            }

            // Random date within last 6 months
            val daysAgo = random.nextInt(0, 180)
            val date = today.minusDays(daysAgo.toLong())

            // Random amount: expenses R$5-500, income R$500-10000
            val amountCents = if (isExpense) {
                random.nextLong(500, 50000) // R$5.00 to R$500.00
            } else {
                random.nextLong(50000, 1000000) // R$500.00 to R$10,000.00
            }

            val description = if (isExpense) {
                expenseDescriptions.random(random)
            } else {
                incomeDescriptions.random(random)
            }

            transactions.add(
                TransactionEntity(
                    accountId = 1, // Default CASH account
                    categoryId = categoryId,
                    type = type,
                    amountCents = amountCents,
                    date = date.toEpochDay(),
                    description = "$description #${index + 1}",
                    createdAt = System.currentTimeMillis() - (daysAgo * 86400000L),
                    updatedAt = System.currentTimeMillis() - (daysAgo * 86400000L)
                )
            )
        }

        // Insert all transactions
        transactions.forEach { transaction ->
            transactionDao.insert(transaction)
        }
    }
}
