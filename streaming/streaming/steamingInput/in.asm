%include "asm_io.inc"
;
; 初始化放入到数据段里的数据
;
segment .data
;
; 这些变量指向用来输出的字符串
;
prompt1 db     "Enter a number: ", 0 ; 不要忘记空结束符
prompt2 db     "Enter another number: ", 0
outmsg1 db     "You entered ", 0
outmsg2 db     " and ", 0
outmsg3 db     ", the sum of these is ", 0

;
; 初始化放入到.bss段里的数据
;
segment .bss
;
; 这个变量指向用来储存输入的双字
;
input1 resd 1
input2 resd 1

;
; 代码放入到.text段
;
segment .text
            global _asm_main
_asm_main:
            enter 0,0 ; 开始运行
            pusha

             mov eax, prompt1 ; 输出提示
             call print_string

             call read_int ; 读整形变量储存到input1
             mov [input1], eax ;

             mov eax, prompt2 ; 输出提示
             call print_string

             call read_int ; 读整形变量储存到input2
             mov [input2], eax ;

             mov eax, [input1] ; eax = 在input1里的双字
             add eax, [input2] ; eax = eax + 在input2里的双字
             mov ebx, eax ; ebx = eax

             dump_regs 1 ; 输出寄存器值
             dump_mem 2, outmsg1, 1 ; 输出内存
;
; 下面分几步输出结果信息
;
             mov eax, outmsg1
             call print_string ; 输出第一条信息
             mov eax, [input1]
             call print_int ; 输出input1
             mov eax, outmsg2
             call print_string ; 输出第二条信息
             mov eax, [input2]
             call print_int ; 输出input2
             mov eax, outmsg3
             call print_string ; 输出第三条信息
             mov eax, ebx
             call print_int ; 输出总数(ebx)
             call print_nl ; 换行

             popa
             mov eax, 0 ; 回到C中
             leave
             ret