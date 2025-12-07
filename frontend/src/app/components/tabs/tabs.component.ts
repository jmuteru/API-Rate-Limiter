import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-tabs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tabs.component.html',
  styleUrls: ['./tabs.component.css']
})
export class TabsComponent {
  @Input() tabs: { label: string }[] = [];
  @Input() activeTabIndex: number = 0;
  @Output() tabChange = new EventEmitter<number>();

  selectTab(index: number) {
    this.activeTabIndex = index;
    this.tabChange.emit(index);
  }

  isActive(index: number): boolean {
    return this.activeTabIndex === index;
  }
}

